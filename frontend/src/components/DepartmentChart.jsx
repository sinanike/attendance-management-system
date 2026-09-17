import {Bar,BarChart,CartesianGrid,Tooltip,XAxis,YAxis} from 'recharts'

export default function DepartmentChart({data}){
  return <article className="panel">
    <div className="panel-head">
      <div><span className="panel-kicker">مقایسه واحدها</span><h2>مقایسه واحدهای سازمانی</h2></div>
      <BuildingIcon/>
    </div>
    <div className="chart-box">
      <BarChart responsive data={data} margin={{top:12,right:5,left:10,bottom:0}} style={{width:'100%',height:'100%'}}>
        <CartesianGrid stroke="#e5ebf2" strokeDasharray="4 4" vertical={false}/>
        <XAxis dataKey="name" interval={0} tick={{fontSize:9,fill:'#657990'}} axisLine={false} tickLine={false}/>
        <YAxis domain={[0,100]} tickFormatter={v=>`${v}%`} width={48} tickMargin={10} tick={{fontSize:9,fill:'#718399'}} axisLine={false} tickLine={false}/>
        <Tooltip formatter={(v)=>[`${v}%`,'نرخ حضور']}/>
        <Bar dataKey="rate" fill="#058db7" radius={[4,4,0,0]} barSize={28}/>
      </BarChart>
    </div>
  </article>
}

function BuildingIcon(){
  return <span style={{color:'#173d6a',display:'grid',placeItems:'center'}}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M3 21h18M6 21V5l6-3v19M18 21V9l-6-2M9 9h.01M9 13h.01M9 17h.01M15 13h.01M15 17h.01"/></svg></span>
}
