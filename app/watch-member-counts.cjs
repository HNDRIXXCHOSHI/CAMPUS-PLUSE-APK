#!/usr/bin/env node
const fs=require('node:fs');
function countActiveMembers(docs) {
  const totals=new Map();
  for (const row of docs) {
    if (row.status!=='ACTIVE'||typeof row.clubId!=='string'||!row.clubId||typeof row.userId!=='string'||!row.userId) continue;
    if(!totals.has(row.clubId))totals.set(row.clubId,new Set());
    totals.get(row.clubId).add(row.userId);
  }
  return new Map([...totals].map(([club,users])=>[club,users.size]));
}
async function main(){
  const args=process.argv.slice(2);const i=args.indexOf('--key');
  if(i<0||!args[i+1])throw Error('Run: node watch-member-counts.cjs --key "PATH_TO_PRIVATE_KEY.json"');
  const credentials=JSON.parse(fs.readFileSync(args[i+1],'utf8'));
  if(credentials.type!=='service_account'||credentials.project_id!=='campus-pulse-ump')throw Error('Use the service-account key for campus-pulse-ump.');
  if(process.env.FIRESTORE_EMULATOR_HOST)throw Error('Unset FIRESTORE_EMULATOR_HOST before using the real database.');
  const {initializeApp,cert,deleteApp}=require('firebase-admin/app');
  const {getFirestore,FieldValue}=require('firebase-admin/firestore');
  const app=initializeApp({credential:cert(credentials),projectId:'campus-pulse-ump'});const db=getFirestore(app);
  let memberships=null,clubs=null,running=false,dirty=false,stopping=false;
  const stops=[];
  async function refresh(){
    dirty=true;if(running||memberships===null||clubs===null||stopping)return;
    running=true;
    try{
      while(dirty&&!stopping){
        dirty=false;
        const counts=countActiveMembers(memberships);
        for(const id of clubs){
          const count=counts.get(id)||0;
          const changed=await db.runTransaction(async tx=>{
            const ref=db.collection('clubs').doc(id);const snap=await tx.get(ref);
            if(!snap.exists||snap.get('memberCount')===count)return false;
            tx.update(ref,{memberCount:count,memberCountUpdatedAt:FieldValue.serverTimestamp()});return true;
          });
          if(changed)console.log(id+': '+count+' active members');
        }
      }
    }catch(e){console.error('Counter update failed:',e.message);process.exitCode=1;await shutdown();}
    finally{running=false;}
  }
  async function shutdown(){if(stopping)return;stopping=true;stops.forEach(stop=>stop());await deleteApp(app);}
  function failed(e){console.error('Listener failed:',e.message);process.exitCode=1;void shutdown();}
  stops.push(db.collection('clubMembers').onSnapshot(s=>{memberships=s.docs.map(d=>d.data());void refresh();},failed));
  // React only to added/deleted clubs; our own count updates do not schedule another pass.
  stops.push(db.collection('clubs').onSnapshot(s=>{const next=s.docs.map(d=>d.id).sort();if(clubs===null||next.join('\n')!==clubs.join('\n')){clubs=next;void refresh();}},failed));
  process.once('SIGINT',()=>{void shutdown();});process.once('SIGTERM',()=>{void shutdown();});
  console.log('Member counter running for campus-pulse-ump. Keep this terminal open during the demonstration.');
  console.log('Only club memberCount and memberCountUpdatedAt are updated. Member details and permissions remain unchanged.');
}
if(require.main===module)main().catch(e=>{console.error(e.message);process.exitCode=1;});
module.exports={countActiveMembers};
