import Image from "next/image";
import Link from "next/link";

const code = `val state = rememberHeatmapState(market)
Heatmap(
  state = state,
  modifier = Modifier.fillMaxSize(),
  colorScale = SignedMetricColorScale(10.0),
)`;

const basePath = process.env.NEXT_PUBLIC_BASE_PATH ?? "";

export default function Home() {
  return <main className="landing">
    <section className="hero">
      <Image className="hero-art" src={`${basePath}/images/volcano-hero.png`} alt="Abstract volcanic heatmap landscape" fill priority sizes="100vw" />
      <div className="hero-shade" />
      <div className="hero-content">
        <p className="eyebrow brand-eyebrow"><Image src={`${basePath}/icon.svg`} width={22} height={22} alt="" aria-hidden /> VOLCANO 2.0</p>
        <h1>Turn changing data<br />into <em>clear terrain.</em></h1>
        <p className="hero-copy">A Kotlin Multiplatform heatmap SDK for dense, hierarchical data. One immutable model. Native-feeling Compose experiences on Android, iOS, and Desktop.</p>
        <div className="hero-actions"><Link className="button primary" href="/en/docs/getting-started">Start building <span>→</span></Link><Link className="button secondary" href="/en/docs/samples">See live samples</Link></div>
        <dl className="hero-stats"><div><dt>3</dt><dd>platforms</dd></div><div><dt>5K</dt><dd>leaf benchmark</dd></div><div><dt>0</dt><dd>forced image loaders</dd></div></dl>
      </div>
    </section>

    <section className="proof"><p>DESIGNED FOR COMPOSE MULTIPLATFORM</p><div><span>Android</span><span>iOS</span><span>Desktop</span></div></section>

    <section className="feature-wrap">
      <div className="section-intro"><p className="eyebrow dark"><span>01</span> DATA, NOT A WIDGET</p><h2>Hierarchy is the<br /><em>navigation model.</em></h2><p>Start with a meaningful overview. Drill into a group when the decision needs detail. Keep leaves useful without turning the first viewport into noise.</p></div>
      <div className="mini-map" aria-label="Heatmap example"><div className="tile gain big">Technology<br /><b>+4.82%</b></div><div className="tile gain">Cloud<br /><b>+2.19%</b></div><div className="tile loss">Energy<br /><b>−1.14%</b></div><div className="tile gain">Health<br /><b>+1.56%</b></div><div className="tile neutral">Other</div></div>
    </section>

    <section className="code-section"><div><p className="eyebrow"><span>02</span> ONE API, THREE HOSTS</p><h2>Build once in<br /><em>commonMain.</em></h2><p>Area comes from <code>value</code>. Color comes from <code>metric</code>. The host owns navigation, images, and constraints.</p><Link href="/en/docs/architecture" className="text-link">Explore the architecture →</Link></div><pre><code>{code}</code></pre></section>

    <section className="closing"><p className="eyebrow dark"><span>03</span> READY FOR DENSE DATA</p><h2>Make the signal<br />impossible to miss.</h2><p>Adaptive content, optional image loading, platform-aware interaction, accessibility semantics, and aggregation for real product data.</p><div className="hero-actions closing-actions"><Link className="button primary" href="/en/docs/getting-started">Read the docs <span>→</span></Link><Link className="button secondary ink" href="/ko/docs">한국어 문서</Link></div></section>
  </main>;
}
