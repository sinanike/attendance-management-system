import {Cell,Pie,PieChart,Tooltip} from 'recharts'

export default function AttendanceStatusChart({data}){
  const total=data.reduce((s,x)=>s+x.value,0)
  return <article className="panel">
    <div className="panel-head">
      <div><span className="panel-kicker">وضعیت امروز</span><h2>وضعیت امروز کارکنان</h2></div>
    </div>
    <div className="donut-layout">
      <div className="donut-wrap">
        <PieChart responsive style={{width:'100%',height:'100%'}}>
          <Pie data={data} dataKey="value" nameKey="name" innerRadius="61%" outerRadius="82%" stroke="#fff" strokeWidth={1}>
            {data.map(x=><Cell key={x.name} fill={x.color}/>)}
          </Pie>
          <Tooltip formatter={(v)=>[`${Number(v).toLocaleString('fa-IR')} نفر`,'']}/>
        </PieChart>
        <div className="donut-center"><strong>{total.toLocaleString('fa-IR')}</strong><span>جمع کل</span></div>
      </div>
      <div className="legend">
        {data.map(x=><div className="legend-row" key={x.name}>
          <span className="legend-name"><span className="legend-dot" style={{background:x.color}}></span>{x.name}</span>
          <span className="legend-value">{x.value.toLocaleString('fa-IR')}</span>
        </div>)}
      </div>
    </div>
  </article>
}
