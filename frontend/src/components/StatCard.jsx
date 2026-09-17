export default function StatCard({title,value,unit,tone,icon:Icon}){
  return <article className={`stat-card tone-${tone}`}>
    <div className="stat-top">
      <div>
        <div className="stat-title">{title}</div>
        <div className="stat-value">{typeof value==='number'?value.toLocaleString('fa-IR'):value}</div>
      </div>
      <span className="stat-icon"><Icon size={20}/></span>
    </div>
    <div className="stat-unit">{unit}</div>
  </article>
}
