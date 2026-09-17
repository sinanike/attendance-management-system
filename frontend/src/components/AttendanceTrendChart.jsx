import {Line,LineChart,CartesianGrid,Tooltip,XAxis,YAxis} from 'recharts'

export default function AttendanceTrendChart({data}){
  return <article className="panel">
    <div className="panel-head">
      <div><span className="panel-kicker">روند حضور</span><h2>روند حضور ۳۰ روز اخیر</h2></div>
    </div>
    <div className="chart-box">
      <LineChart responsive data={data} margin={{top:12,right:7,left:10,bottom:0}} style={{width:'100%',height:'100%'}}>
        <CartesianGrid stroke="#e5ebf2" strokeDasharray="4 4" vertical={false}/>
        <XAxis dataKey="day" tick={{fontSize:9,fill:'#687c92'}} axisLine={false} tickLine={false}/>
        <YAxis domain={[0,100]} tickFormatter={v=>`${v}%`} width={48} tickMargin={10} tick={{fontSize:9,fill:'#718399'}} axisLine={false} tickLine={false}/>
        <Tooltip formatter={(v)=>[`${v}%`,'نرخ حضور']}/>
        <Line type="monotone" dataKey="rate" stroke="#058db7" strokeWidth={2.4} dot={{r:3,fill:'#058db7'}} activeDot={{r:5}}/>
      </LineChart>
    </div>
  </article>
}
