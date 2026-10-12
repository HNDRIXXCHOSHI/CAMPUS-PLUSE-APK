"use strict";

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const {
  getFirestore,
  FieldValue,
  Timestamp
} = require("firebase-admin/firestore");
const { getStorage } = require("firebase-admin/storage");

initializeApp();

const db = getFirestore();

function text(value, field, maximum, required = true) {
  if (typeof value !== "string") {
    throw new HttpsError(
      "invalid-argument",
      `${field} must be text.`
    );
  }

  const result = value.trim();

  if ((required && !result) || result.length > maximum) {
    throw new HttpsError(
      "invalid-argument",
      `${field} must contain ${required ? "1" : "0"}–${maximum} characters.`
    );
  }

  return result;
}

function documentId(value, field = "ID") {
  const result = text(value, field, 1500);

  if (
    result.includes("/") ||
    result === "." ||
    result === ".." ||
    Buffer.byteLength(result, "utf8") > 1500
  ) {
    throw new HttpsError(
      "invalid-argument",
      `Invalid ${field}.`
    );
  }

  return result;
}

function parseEventDate(date, time) {
  if (
    !/^\d{4}-\d{2}-\d{2}$/.test(date) ||
    !/^\d{2}:\d{2}$/.test(time)
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Use date YYYY-MM-DD and time HH:mm."
    );
  }

  const value = new Date(`${date}T${time}:00+02:00`);
  const [year, month, day] = date.split("-").map(Number);
  const [hour, minute] = time.split(":").map(Number);
  const check = new Date(Date.UTC(year, month - 1, day));

  if (
    !Number.isFinite(value.getTime()) ||
    check.getUTCFullYear() !== year ||
    check.getUTCMonth() !== month - 1 ||
    check.getUTCDate() !== day ||
    hour > 23 ||
    minute > 59
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Choose a valid date and time."
    );
  }

  return value;
}

function memberTransition(current, operation) {
  const allowed = {
    approve: ["PENDING"],
    reject: ["PENDING"],
    suspend: ["ACTIVE"],
    unsuspend: ["SUSPENDED"],
    remove: ["ACTIVE", "SUSPENDED", "PENDING"]
  };

  if (!allowed[operation]?.includes(current)) {
    throw new HttpsError(
      "failed-precondition",
      "Member status changed. Refresh and try again."
    );
  }

  return {
    approve: "ACTIVE",
    reject: "REJECTED",
    suspend: "SUSPENDED",
    unsuspend: "ACTIVE",
    remove: "REMOVED"
  }[operation];
}

async function authority(transaction, uid, clubId, ownerRequired = false) {
  const accessRef = db.collection("adminAccess").doc(uid);
  const clubRef = db.collection("clubs").doc(clubId);
  const profileRef = db.collection("users").doc(uid);

  const [access, club, profile] = await Promise.all([
    transaction.get(accessRef),
    transaction.get(clubRef),
    transaction.get(profileRef)
  ]);

  if (
    !access.exists ||
    access.get("isActive") !== true ||
    access.get("clubId") !== clubId ||
    !club.exists ||
    club.get("isActive") !== true ||
    club.get("status") !== "APPROVED" ||
    (
      profile.exists &&
      profile.get("accountStatus") !== "ACTIVE"
    )
  ) {
    throw new HttpsError(
      "permission-denied",
      "You have no active admin assignment for this club."
    );
  }

  if (
    ownerRequired &&
    access.get("canManageAdmins") !== true
  ) {
    throw new HttpsError(
      "permission-denied",
      "Only the designated club owner can manage admins."
    );
  }

  return {
    access,
    club,
    accessRef,
    clubRef
  };
}

async function validateMedia(value, clubId, kind) {
  const url = text(value ?? "", "Media URL", 3000, false);

  if (!url) return "";

  let parsed;

  try {
    parsed = new URL(url);
  } catch (_) {
    throw new HttpsError(
      "invalid-argument",
      "Invalid media URL."
    );
  }

  if (
    parsed.protocol !== "https:" ||
    parsed.hostname !== "firebasestorage.googleapis.com"
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Use media uploaded by this application."
    );
  }

  const match = parsed.pathname.match(
    /^\/v0\/b\/([^/]+)\/o\/(.+)$/
  );

  const bucket = getStorage().bucket();

  if (
    !match ||
    decodeURIComponent(match[1]) !== bucket.name
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Media is from another Firebase project."
    );
  }

  const path = decodeURIComponent(match[2]);
  const prefix = `clubMedia/${clubId}/`;

  if (
    !path.startsWith(prefix) ||
    !path.slice(prefix.length) ||
    path.slice(prefix.length).includes("/")
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Media is not assigned to this club."
    );
  }

  const [metadata] = await bucket.file(path).getMetadata();
  const expectedType = kind === "VIDEO" ? "video/" : "image/";
  const maximum = kind === "VIDEO"
    ? 100 * 1024 * 1024
    : 10 * 1024 * 1024;

  if (
    !String(metadata.contentType).startsWith(expectedType) ||
    Number(metadata.size) > maximum
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Media type or file size is invalid."
    );
  }

  return url;
}

exports.clubAdminAction = onCall(
  {
    region: "us-central1",
    timeoutSeconds: 120
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Sign in again."
      );
    }

    const uid = request.auth.uid;
    const data = request.data ?? {};

    try {
      const clubId = documentId(data.clubId, "Club ID");
      const action = text(data.action, "Action", 50);

      await db.runTransaction(
        transaction => authority(transaction, uid, clubId)
      );

      if (action === "members") {
        const memberships = await db.collection("clubMembers")
          .where("clubId", "==", clubId)
          .get();

        const visible = memberships.docs.filter(
          document => ["ACTIVE", "PENDING", "SUSPENDED"]
            .includes(document.get("status"))
        );

        if (!visible.length) return { items: [] };

        const userIds = visible.map(
          document => documentId(document.get("userId"), "User ID")
        );

        const profiles = [];
        const assignments = [];

        // Read in batches to avoid one oversized getAll request.
        for (let offset = 0; offset < userIds.length; offset += 200) {
          const batch = userIds.slice(offset, offset + 200);

          const [batchProfiles, batchAssignments] = await Promise.all([
            db.getAll(
              ...batch.map(id => db.collection("users").doc(id))
            ),
            db.getAll(
              ...batch.map(id => db.collection("adminAccess").doc(id))
            )
          ]);

          profiles.push(...batchProfiles);
          assignments.push(...batchAssignments);
        }

        return {
          items: visible.map((document, index) => ({
            id: document.id,
            userId: userIds[index],
            name: profiles[index].get("name") ?? "Member",
            email: profiles[index].get("email") ?? "",
            studentNumber:
              profiles[index].get("studentNumber") ?? "Not provided",
            course: profiles[index].get("course") ?? "Not provided",
            year: profiles[index].get("year") ?? "Not provided",
            status: document.get("status"),
            role:
              assignments[index].get("isActive") === true &&
              assignments[index].get("clubId") === clubId
                ? "Admin"
                : "Member",
            joinedAt: document.get("joinedAt")?.toMillis() ?? 0
          }))
        };
      }

      if (action === "admins") {
        const assignments = await db.collection("adminAccess")
          .where("clubId", "==", clubId)
          .where("isActive", "==", true)
          .get();

        if (assignments.empty) return { items: [] };

        const profiles = await db.getAll(
          ...assignments.docs.map(
            document => db.collection("users").doc(document.id)
          )
        );

        return {
          items: assignments.docs.map((document, index) => ({
            uid: document.id,
            name: profiles[index].get("name") ?? "Club admin",
            owner: document.get("canManageAdmins") === true,
            self: document.id === uid
          }))
        };
      }

      if (action === "notifications") {
        const [notices, reads] = await Promise.all([
          db.collection("adminNotifications")
            .where("clubId", "==", clubId)
            .limit(200)
            .get(),
          db.collection("adminAccess")
            .doc(uid)
            .collection("notificationReads")
            .get()
        ]);

        const readIds = new Set(
          reads.docs.map(document => document.id)
        );

        return {
          items: notices.docs.map(document => ({
            id: document.id,
            title: document.get("title") ?? "Announcement",
            message: document.get("message") ?? "",
            read: readIds.has(document.id),
            createdAt: document.get("createdAt")?.toMillis() ?? 0
          })).sort((a, b) => b.createdAt - a.createdAt)
        };
      }

      if (action === "readNotification") {
        const noticeId = documentId(data.id);

        await db.runTransaction(async transaction => {
          await authority(transaction, uid, clubId);

          const notice = await transaction.get(
            db.collection("adminNotifications").doc(noticeId)
          );

          if (
            !notice.exists ||
            notice.get("clubId") !== clubId
          ) {
            throw new HttpsError(
              "permission-denied",
              "This notification is not for your club."
            );
          }

          transaction.set(
            db.collection("adminAccess")
              .doc(uid)
              .collection("notificationReads")
              .doc(noticeId),
            {
              readAt: FieldValue.serverTimestamp()
            }
          );
        });

        return { ok: true };
      }

      if (action === "eventSave") {
        const eventId = documentId(data.id, "Event ID");
        const date = text(data.date, "Date", 10);
        const time = text(data.time, "Time", 5);
        const when = parseEventDate(date, time);
        const kind = data.mediaType === "VIDEO" ? "VIDEO" : "PHOTO";
        const mediaUrl = await validateMedia(
          data.mediaUrl,
          clubId,
          kind
        );

        const rsvpUrl = text(
          data.link ?? "",
          "RSVP link",
          2000,
          false
        );

        if (rsvpUrl) {
          let parsed;

          try {
            parsed = new URL(rsvpUrl);
          } catch (_) {
            throw new HttpsError(
              "invalid-argument",
              "Enter a valid RSVP URL."
            );
          }

          if (!["https:", "http:"].includes(parsed.protocol)) {
            throw new HttpsError(
              "invalid-argument",
              "RSVP link must start with https:// or http://."
            );
          }
        }

        const values = {
          title: text(data.title, "Title", 150),
          description: text(
            data.description,
            "Description",
            5000,
            false
          ),
          category: text(data.category, "Category", 100),
          location: text(data.venue, "Venue", 300),
          date,
          time,
          eventDate: Timestamp.fromDate(when),
          rsvpEnabled: data.rsvpEnabled === true,
          rsvpUrl,
          mediaType: kind,
          mediaUrl,
          imageUrl: kind === "PHOTO" ? mediaUrl : "",
          videoUrl: kind === "VIDEO" ? mediaUrl : "",
          status: "PUBLISHED",
          isActive: true,
          updatedAt: FieldValue.serverTimestamp()
        };

        await db.runTransaction(async transaction => {
          const { club } = await authority(
            transaction,
            uid,
            clubId
          );

          const reference = db.collection("events").doc(eventId);
          const existing = await transaction.get(reference);

          if (
            existing.exists &&
            existing.get("clubId") !== clubId
          ) {
            throw new HttpsError(
              "permission-denied",
              "This event belongs to another club."
            );
          }

          transaction.set(
            reference,
            {
              ...values,
              clubId,
              clubName: club.get("name"),
              clubShortName:
                club.get("shortName") ?? club.get("Shortname") ?? "",
              ...(!existing.exists ? {
                authorId: uid,
                createdAt: FieldValue.serverTimestamp()
              } : {})
            },
            { merge: true }
          );
        });

        return { id: eventId };
      }

      if (action === "eventDelete") {
        const eventId = documentId(data.id);

        await db.runTransaction(async transaction => {
          await authority(transaction, uid, clubId);

          const reference = db.collection("events").doc(eventId);
          const event = await transaction.get(reference);

          if (
            !event.exists ||
            event.get("clubId") !== clubId
          ) {
            throw new HttpsError(
              "not-found",
              "Event not found in this club."
            );
          }

          transaction.delete(reference);
        });

        return { ok: true };
      }

      if (action === "postPublish") {
        const type = text(data.type, "Post type", 10);

        if (!["TEXT", "PHOTO", "VIDEO"].includes(type)) {
          throw new HttpsError(
            "invalid-argument",
            "Invalid post type."
          );
        }

        const caption = text(
          data.text ?? "",
          "Post text",
          5000,
          type === "TEXT"
        );

        const mediaUrl = type === "TEXT"
          ? ""
          : await validateMedia(data.mediaUrl, clubId, type);

        if (type !== "TEXT" && !mediaUrl) {
          throw new HttpsError(
            "invalid-argument",
            "Select a media file."
          );
        }

        const reference = db.collection("posts")
          .doc(documentId(data.id, "Post ID"));

        await db.runTransaction(async transaction => {
          const { club } = await authority(
            transaction,
            uid,
            clubId
          );

          const existing = await transaction.get(reference);

          if (existing.exists) {
            if (
              existing.get("authorId") !== uid ||
              existing.get("clubId") !== clubId
            ) {
              throw new HttpsError(
                "already-exists",
                "Post ID already in use."
              );
            }

            // Retrying a completed publish does not duplicate the post.
            return;
          }

          transaction.set(reference, {
            clubId,
            clubName: club.get("name"),
            clubShortName:
              club.get("shortName") ?? club.get("Shortname") ?? "",
            authorId: uid,
            type,
            text: caption,
            imageUrl: type === "PHOTO" ? mediaUrl : "",
            videoUrl: type === "VIDEO" ? mediaUrl : "",
            mediaUrl,
            status: "PUBLISHED",
            isActive: true,
            createdAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp()
          });
        });

        return { id: reference.id };
      }

      if (
        action === "clubSave" ||
        action === "joining" ||
        action === "notificationPreference"
      ) {
        const values = {};

        if (action === "clubSave") {
          values.name = text(data.name, "Club name", 150);
          values.description = text(
            data.description,
            "Description",
            5000,
            false
          );
          values.history = text(
            data.history,
            "History",
            10000,
            false
          );

          const category = text(data.category, "Category", 50);

          const categories = {
            "SPORT": "SPORT",
            "Sport": "SPORT",
            "CULTURE": "CULTURE",
            "Culture & Arts": "CULTURE",
            "ADVOCACY": "ADVOCACY",
            "Advocacy & Politics": "ADVOCACY",
            "IDENTITY": "IDENTITY",
            "Identity & Community": "IDENTITY",
            "ACADEMIC": "ACADEMIC",
            "Faith & Academic": "ACADEMIC",
            "Academic": "ACADEMIC"
          };

          if (!Object.prototype.hasOwnProperty.call(categories, category)) {
            throw new HttpsError(
              "invalid-argument",
              "Use SPORT, CULTURE, ADVOCACY, IDENTITY or ACADEMIC."
            );
          }

          values.category = categories[category];

          if (data.bannerUrl) {
            values.bannerUrl = await validateMedia(
              data.bannerUrl,
              clubId,
              "PHOTO"
            );
          }

          if (data.profileImageUrl) {
            values.profileImageUrl = await validateMedia(
              data.profileImageUrl,
              clubId,
              "PHOTO"
            );
          }
        } else if (typeof data.enabled !== "boolean") {
          throw new HttpsError(
            "invalid-argument",
            "Enabled must be true or false."
          );
        }

        await db.runTransaction(async transaction => {
          const { clubRef, accessRef } = await authority(
            transaction,
            uid,
            clubId
          );

          if (action === "notificationPreference") {
            transaction.update(accessRef, {
              notificationsEnabled: data.enabled
            });
          } else {
            transaction.update(clubRef, {
              ...values,
              ...(action === "joining"
                ? { joiningOpen: data.enabled }
                : {}),
              updatedAt: FieldValue.serverTimestamp()
            });
          }
        });

        return { ok: true };
      }

      if (action === "member") {
        const membershipId = documentId(data.id);
        const operation = text(data.operation, "Operation", 20);

        await db.runTransaction(async transaction => {
          await authority(transaction, uid, clubId);

          const reference = db.collection("clubMembers")
            .doc(membershipId);
          const member = await transaction.get(reference);

          if (
            !member.exists ||
            member.get("clubId") !== clubId
          ) {
            throw new HttpsError(
              "not-found",
              "Member not found in this club."
            );
          }

          const memberUid = documentId(member.get("userId"));
          const assignment = await transaction.get(
            db.collection("adminAccess").doc(memberUid)
          );

          if (
            assignment.get("isActive") === true &&
            assignment.get("clubId") === clubId
          ) {
            throw new HttpsError(
              "failed-precondition",
              "Revoke admin access before changing this member's status."
            );
          }

          const status = memberTransition(
            member.get("status"),
            operation
          );

          transaction.update(reference, {
            status,
            updatedAt: FieldValue.serverTimestamp()
          });

          const restriction = db.collection("clubRestrictions")
            .doc(clubId)
            .collection("users")
            .doc(memberUid);

          if (["SUSPENDED", "REMOVED", "REJECTED"].includes(status)) {
            transaction.set(restriction, {
              status,
              updatedAt: FieldValue.serverTimestamp()
            });
          } else {
            transaction.delete(restriction);
          }
        });

        return { ok: true };
      }

      if (action === "adminGrant" || action === "adminRevoke") {
        const target = documentId(data.userId, "User ID");

        if (target === uid) {
          throw new HttpsError(
            "failed-precondition",
            "You cannot change your own admin assignment here."
          );
        }

        await db.runTransaction(async transaction => {
          const { access } = await authority(
            transaction,
            uid,
            clubId,
            true
          );

          const reference = db.collection("adminAccess").doc(target);
          const previous = await transaction.get(reference);

          const memberships = await transaction.get(
            db.collection("clubMembers")
              .where("clubId", "==", clubId)
              .where("userId", "==", target)
              .where("status", "==", "ACTIVE")
          );

          const restriction = await transaction.get(
            db.collection("clubRestrictions")
              .doc(clubId)
              .collection("users")
              .doc(target)
          );

          if (
            previous.exists &&
            previous.get("isActive") === true &&
            previous.get("clubId") !== clubId
          ) {
            throw new HttpsError(
              "failed-precondition",
              "This user is assigned to another club."
            );
          }

          if (previous.get("canManageAdmins") === true) {
            throw new HttpsError(
              "permission-denied",
              "Only the platform operator can change a club owner's assignment."
            );
          }

          if (action === "adminGrant") {
            if (memberships.empty || restriction.exists) {
              throw new HttpsError(
                "failed-precondition",
                "Only unrestricted active members can become admins."
              );
            }

            transaction.set(reference, {
              clubId,
              clubCode: access.get("clubCode"),
              isActive: true,
              canManageAdmins: false,
              notificationsEnabled: true,
              updatedAt: FieldValue.serverTimestamp()
            });
          } else {
            if (
              !previous.exists ||
              previous.get("clubId") !== clubId
            ) {
              throw new HttpsError(
                "not-found",
                "Admin not found in this club."
              );
            }

            transaction.update(reference, {
              isActive: false,
              updatedAt: FieldValue.serverTimestamp()
            });
          }
        });

        return { ok: true };
      }

      throw new HttpsError(
        "invalid-argument",
        "Unknown action."
      );
    } catch (error) {
      if (error instanceof HttpsError) throw error;

      console.error("clubAdminAction failed", error);

      throw new HttpsError(
        "internal",
        "Action failed. Retry or check the Firebase function logs."
      );
    }
  }
);