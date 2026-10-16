import { useState } from 'react'

const API = 'http://localhost:8080/api/evaluate'
const examples = ['(2 + 3) * 4', '2^3^2', '-5 + 2.5 * 4', '10 / (2 + 3)']

function AstNode({ node }) {
  if (!node) return null
  return (
    <div className="tree-node">
      <div className="node-card">
        <span className="node-kind">{node.kind}</span>
        <strong>{node.value}</strong>
      </div>
      {(node.left || node.right) && (
        <div className="tree-children">
          {node.left && <div className="tree-child"><small>left</small><AstNode node={node.left} /></div>}
          {node.right && <div className="tree-child"><small>right</small><AstNode node={node.right} /></div>}
        </div>
      )}
    </div>
  )
}

export default function App() {
  const [expression, setExpression] = useState('(2 + 3) * 4')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [history, setHistory] = useState(() => {
    try { return JSON.parse(localStorage.getItem('expression-history') || '[]') } catch { return [] }
  })

  async function evaluate(e) {
    e?.preventDefault()
    setLoading(true); setError(''); setResult(null)
    try {
      const response = await fetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ expression })
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.error || 'Could not evaluate expression.')
      setResult(data)
      const item = { expression: data.expression, result: data.result }
      const updated = [item, ...history.filter(h => h.expression !== item.expression)].slice(0, 8)
      setHistory(updated)
      localStorage.setItem('expression-history', JSON.stringify(updated))
    } catch (err) {
      setError(err.message.includes('Failed to fetch')
        ? 'Cannot connect to backend. Start Spring Boot on port 8080 and try again.'
        : err.message)
    } finally { setLoading(false) }
  }

  function clearHistory() {
    setHistory([])
    localStorage.removeItem('expression-history')
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div className="brand-mark">∑</div>
        <div><div className="eyebrow">COMPILER DESIGN PROJECT</div><h1>Expression Evaluator</h1></div>
        <div className="status-pill"><span /> Compiler ready</div>
      </header>

      <section className="hero">
        <div>
          <p className="eyebrow">LEXICAL ANALYSIS · PARSING · AST</p>
          <h2>Math, processed<br /><em>like a compiler.</em></h2>
          <p className="hero-copy">Enter an expression and explore how the compiler tokenizes, parses, and evaluates it.</p>
        </div>
        <div className="hero-symbol" aria-hidden="true"><span>(</span><b>∑</b><span>)</span></div>
      </section>

      <section className="workspace">
        <div className="panel input-panel">
          <div className="panel-heading"><div><span className="step">01</span><h3>Input expression</h3></div><span className="tag">SOURCE</span></div>
          <form onSubmit={evaluate}>
            <label htmlFor="expression">MATHEMATICAL EXPRESSION</label>
            <textarea id="expression" value={expression} onChange={e => setExpression(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) evaluate(e) }}
              placeholder="e.g. (2 + 3) * 4" rows="3" />
            <div className="operators"><span>Supported:</span><code>+ − × ÷ ^ ( )</code><span>Ctrl + Enter to run</span></div>
            <button className="primary-button" type="submit" disabled={loading}>
              {loading ? 'Evaluating…' : '▶  Compile & Evaluate'}
            </button>
          </form>
          <div className="examples"><span>TRY AN EXAMPLE</span>{examples.map(ex =>
            <button key={ex} onClick={() => setExpression(ex)}>{ex}</button>)}</div>
          {error && <div className="error-box"><strong>Compilation error</strong><p>{error}</p></div>}
          {result && <div className="result-box"><div><span>Evaluation result</span><small>SUCCESS</small></div>
            <strong>{Number(result.result.toPrecision(12)).toString()}</strong>
            <code>{result.expression}</code></div>}
        </div>

        <div className="panel tokens-panel">
          <div className="panel-heading"><div><span className="step">02</span><h3>Lexical analysis</h3></div><span className="tag">{result?.tokens?.length ?? 0} TOKENS</span></div>
          <p className="muted">The lexer converts characters into meaningful tokens.</p>
          {result ? <div className="token-list">{result.tokens.map((token, i) =>
            <div className="token-row" key={`${token.position}-${i}`}>
              <span className="token-index">{String(i + 1).padStart(2, '0')}</span>
              <code>{token.lexeme}</code><span className="token-type">{token.type}</span>
              <span className="token-position">@{Number(token.position) + 1}</span>
            </div>)}</div>
            : <div className="empty-state"><span>⌁</span><p>Tokens will appear here after evaluation.</p></div>}
        </div>
      </section>

      <section className="panel ast-panel">
        <div className="panel-heading"><div><span className="step">03</span><h3>Abstract Syntax Tree</h3></div><span className="tag">AST</span></div>
        <p className="muted">The parser builds a tree that represents the expression structure and operator precedence.</p>
        {result ? <div className="ast-scroll"><AstNode node={result.ast} /></div>
          : <div className="empty-state"><span>⌘</span><p>Run an expression to generate its syntax tree.</p></div>}
      </section>

      <section className="panel history-panel">
        <div className="panel-heading"><div><span className="step">04</span><h3>Recent history</h3></div>
          <button className="text-button" onClick={clearHistory} disabled={!history.length}>Clear history</button></div>
        {history.length ? <div className="history-list">{history.map((item, i) =>
          <button className="history-item" key={`${item.expression}-${i}`} onClick={() => setExpression(item.expression)}>
            <code>{item.expression}</code><span>=</span><strong>{Number(Number(item.result).toPrecision(12)).toString()}</strong><small>↗</small>
          </button>)}</div> : <p className="muted">Your evaluated expressions will be saved in this browser.</p>}
      </section>

      <footer><span>EXPRESSION EVALUATOR / CALCULATOR COMPILER</span><span>React · Spring Boot · Recursive-descent parser</span></footer>
    </main>
  )
}
