// Measures Node calculation and SSR work, not browser frame time.
const {performance}=require('node:perf_hooks');
process.env.NODE_ENV = 'production';
const base = require('node:path').resolve(__dirname, '..');
const {computeHeatmapLayout,Heatmap}=require(base+'/dist/index.cjs');
const React=require(base+'/node_modules/react');
const {renderToStaticMarkup}=require(base+'/node_modules/react-dom/server');
function tree(n,groups=20){const leaves=Array.from({length:n},(_,i)=>({id:'leaf-'+i,label:'Leaf '+i,value:((i*1103+97)%10000)+1,metric:((i*313)%2001-1000)/100}));return {id:'root',label:'Root',value:0,children:groups===1?leaves:Array.from({length:groups},(_,i)=>({id:'group-'+i,label:'Group '+i,value:0,children:leaves.slice(Math.floor(n*i/groups),Math.floor(n*(i+1)/groups))}))};}
function bench(name,fn,warm=15,count=40){for(let i=0;i<warm;i++)fn();let times=[];let result;for(let i=0;i<count;i++){const start=performance.now();result=fn();times.push(performance.now()-start);}times.sort((a,b)=>a-b);const row={name,medianMs:+times[Math.floor(count/2)].toFixed(3),p95Ms:+times[Math.floor((count-1)*.95)].toFixed(3)};console.log(JSON.stringify(row));return {row,result};}
const rows=[];const options={width:1080,height:1920};
for(const n of [100,500,1000,5000]) {const data=tree(n);rows.push(bench('adapter-grouped-'+n,()=>computeHeatmapLayout(data,options)).row);const ssr=bench('SSR-grouped-'+n,()=>renderToStaticMarkup(React.createElement(Heatmap,{data,...options,onLeafClick:()=>{}})),3,12);rows.push({...ssr.row,svgElements:(ssr.result.match(/<[a-zA-Z][^/\s>]*/g)||[]).length,markupBytes:Buffer.byteLength(ssr.result)});}
for(const n of [1000,5000,10000]) {const data=tree(n,1);rows.push(bench('adapter-flat-'+n,()=>computeHeatmapLayout(data,options)).row);}
function chain(depth){let node={id:'leaf',label:'Leaf',value:1,metric:1};for(let i=0;i<depth;i++)node={id:'g'+i,label:'Group',value:0,children:[node]};return node;}
for(const depth of [20,100,300]){const data=chain(depth);rows.push(bench('adapter-chain-'+depth,()=>computeHeatmapLayout(data,options),3,12).row);}
console.log(JSON.stringify({node:process.version,platform:process.platform,arch:process.arch,rows},null,2));
