import {Sparkles} from 'lucide-react'

export default function SmartAnalysis({
  analysis,
  available,
  loading,
}){
  return <article className="panel">

    <div className="panel-head">

      <div>

        <span className="panel-kicker">
          تحلیل هوشمند
        </span>

        <h2>
          تحلیل هوشمند
        </h2>

      </div>

      <Sparkles
        size={18}
        color="#164a7a"
      />

    </div>

    <div className="smart-box smart-box-live">

      {
        loading

          ? 'در حال دریافت تحلیل هوشمند...'

          : analysis

            ? analysis

            : available === false

              ? 'سرویس تحلیل هوشمند پیکربندی نشده است.'

              : 'تحلیل هوشمندی برای نمایش وجود ندارد.'
      }

    </div>

  </article>
}
