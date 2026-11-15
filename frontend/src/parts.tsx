import { useEffect, useState, type FormEvent } from 'react';
import { ArrowUpRight, ExternalLink, Layers } from 'lucide-react';
import { api, byId, message, usd, type Community, type Feedback, type Loadout, type Product, type ProductFields } from './api';

export function useData<T>(url: string | null, revision = 0) {
  const [state, set] = useState<{ data: T | null; error: string; loading: boolean }>({ data: null, error: '', loading: true });
  useEffect(() => {
    if (!url) { set({ data: null, error: '', loading: false }); return; }
    const control = new AbortController();
    set({ data: null, error: '', loading: true });
    api<T>(url, 'GET', undefined, control.signal).then(data => {
      if (!control.signal.aborted) set({ data, error: '', loading: false });
    }).catch(error => {
      if (!control.signal.aborted) set({ data: null, error: message(error), loading: false });
    });
    return () => control.abort();
  }, [url, revision]);
  return state;
}
export function ErrorText({ text }: { text: string }) {
  return text ? <p className="error" role="alert">{text}</p> : null;
}
export function Price({ loadout }: { loadout: Loadout }) {
  if (loadout.knownTotal === null) return <span className="price">No prices available</span>;
  return <span className="price">{loadout.missingPriceCount ? 'Known subtotal' : 'Total'}: {usd(loadout.knownTotal)}
    {loadout.missingPriceCount > 0 && <small>{loadout.missingPriceCount} product {loadout.missingPriceCount === 1 ? 'price' : 'prices'} missing</small>}</span>;
}
export function Cards({ items, empty }: { items: Loadout[]; empty: string }) {
  if (!items.length) return <div className="empty"><Layers size={28}/><p>{empty}</p></div>;
  return <div className="grid">{items.map((item,i) => <a className="loadout-card" key={item.id} href={'#loadout/'+item.id}>
    <div className={'card-art color-'+i%4}><Layers size={52} strokeWidth={1}/><span>{item.category || 'Collection'}</span><span className="card-arrow"><ArrowUpRight size={18}/></span></div>
    <div className="card-info"><span className="tiny-tag">{item.draft ? 'Draft' : item.category || 'Uncategorized'}</span>
      <h3>{item.title}</h3><p>{item.description || 'No description provided.'}</p>
      <Price loadout={item}/><div className="card-meta">by {item.ownerUsername}</div>
    </div></a>)}</div>;
}
export const emptyProduct: ProductFields = { name:'', url:null, note:null, store:null, price:null, availability:null };
export function ProductFieldsEditor({ value, change }: { value: ProductFields; change: (value: ProductFields) => void }) {
  return <><div className="field-pair">
      <label>Product name<input required maxLength={120} value={value.name} onChange={e=>change({...value,name:e.target.value})}/></label>
      <label>Storefront<input maxLength={120} value={value.store??''} onChange={e=>change({...value,store:e.target.value||null})}/></label>
    </div>
    <label>Store link<input type="url" maxLength={2048} placeholder="https://store.example/product" value={value.url??''} onChange={e=>change({...value,url:e.target.value||null})}/></label>
    <div className="field-pair">
      <label>Price (USD)<input type="number" min="0" step="0.01" max="9999999999.99" value={value.price??''} onChange={e=>change({...value,price:e.target.value===''?null:e.target.valueAsNumber})}/></label>
      <label>Availability<select value={value.availability??''} onChange={e=>change({...value,availability:e.target.value||null})}>
        <option value="">Unknown</option><option>In stock</option><option>Out of stock</option></select></label>
    </div>
    <label>Note<textarea maxLength={1000} rows={2} value={value.note??''} onChange={e=>change({...value,note:e.target.value||null})}/></label></>;
}
export function Products({ id, owner, revision, changed }: { id: string; owner: boolean; revision: number; changed: () => void }) {
  const path = byId(id) + '/products';
  const data = useData<Product[]>(path, revision);
  const [edit,setEdit] = useState<string|null>(null);
  const [form,setForm] = useState<ProductFields>(emptyProduct);
  const [busy,setBusy] = useState(false);
  const [error,setError] = useState('');
  async function save(e:FormEvent) {
    e.preventDefault(); setBusy(true); setError('');
    try {
      await api(path+(edit?'/'+edit:''), edit?'PUT':'POST',form);
      setEdit(null);setForm(emptyProduct);changed();
    } catch(e) {setError(message(e));} finally {setBusy(false);}
  }
  async function remove(product:Product) {
    if (!confirm('Remove '+product.name+'?')) return;
    setBusy(true);setError('');
    try {await api(path+'/'+product.id,'DELETE');if(edit===product.id){setEdit(null);setForm(emptyProduct);}changed();}
    catch(e){setError(message(e));}finally{setBusy(false);}
  }
  return <section className="products"><h2>Products in this loadout</h2>
    <ErrorText text={data.error}/>
    {data.loading ? <p role="status">Loading products…</p> : data.data && <div className="product-list">
      {!data.data.length && <p className="product-empty">No products have been added.</p>}
      {data.data.map(p=><article key={p.id} className="product-card">
        <div className="product-icon"><Layers size={20}/></div>
        <div className="product-copy"><h3>{p.name}</h3>
        <p>{p.store || 'Store not specified'} · {p.price===null?'Price unavailable':usd(p.price)} · {p.availability || 'Availability unknown'}</p>
        {p.note&&<p>{p.note}</p>}{p.url?<a href={p.url} target="_blank" rel="noopener noreferrer">Visit product link <ExternalLink size={14}/></a>:<p>No store link provided.</p>}</div>
        {owner&&<div className="action-row"><button className="button outline small" disabled={busy} onClick={()=>{setEdit(p.id);setForm(p);setError('');}}>Edit product</button>
        <button className="button outline small" disabled={busy} onClick={()=>void remove(p)}>Remove</button></div>}
      </article>)}</div>}
    {owner&&<form className="product-form" onSubmit={save}>
      <h3>{edit?'Edit product':'Add a product'}</h3>
      <p>Enter details manually. Only the name is required.</p>
      <ProductFieldsEditor value={form} change={setForm}/>
      <div className="action-row"><button className="button dark" disabled={busy}>{edit?'Save product':'Add product'}</button>
      {edit&&<button type="button" className="button outline" disabled={busy} onClick={()=>{setEdit(null);setForm(emptyProduct);}}>Cancel</button>}</div>
    </form>}
    <ErrorText text={error}/>
  </section>;
}
function FeedbackList({ label, entries, signedIn, submit, busy }: {
  label: 'Comments'|'Reviews'; entries: Feedback[]; signedIn: boolean; submit: (body:string)=>Promise<boolean>; busy: boolean;
}) {
  const [body,setBody] = useState('');
  const noun=label==='Comments'?'comment':'review';
  return <div><h3>{label}</h3>
    {entries.length===0&&<p>No {label.toLowerCase()} yet.</p>}
    {entries.map(x=><article className="feedback" key={x.id}><strong>@{x.username}</strong><p>{x.body}</p></article>)}
    {signedIn&&<form onSubmit={async e=>{e.preventDefault();if(await submit(body))setBody('');}}>
      <label>Write a {noun}<textarea required maxLength={3000} rows={2} value={body} onChange={e=>setBody(e.target.value)}/></label>
      <button className="button dark small" disabled={busy||!body.trim()}>Post {noun}</button>
    </form>}
  </div>;
}
export function CommunityPanel({ id, signedIn, signIn, authRevision }: {
  id:string; signedIn:boolean; signIn:()=>void; authRevision:string;
}) {
  const [revision,setRevision]=useState(0);
  const [value,setValue]=useState<Community|null>(null);
  const [error,setError]=useState('');
  const [busy,setBusy]=useState(false);
  const path=byId(id);
  useEffect(()=>{
    const c=new AbortController();
    api<Community>(path+'/community','GET',undefined,c.signal).then(setValue).catch(e=>{if(!c.signal.aborted)setError(message(e));});
    return ()=>c.abort();
  },[path,authRevision,revision]);
  async function send(action:string,method:string,body?:object) {
    if(!signedIn){signIn();return false;}
    setBusy(true);setError('');
    try {
      await api(path+'/'+action,method,body);
      const updated=await api<Community>(path+'/community');
      setValue(updated);
      return true;
    } catch(e) {setError(message(e));return false;} finally {setBusy(false);}
  }
  return <section className="products community"><h2>Community</h2>
    {value?<><div className="action-row"><button className="button outline small" aria-pressed={signedIn&&value.liked} disabled={busy} onClick={()=>void send('like',signedIn&&value.liked?'DELETE':'PUT')}>{signedIn&&value.liked?'Unlike':'Like'} ({value.likes})</button>
      <button className="button outline small" aria-pressed={signedIn&&value.bookmarked} disabled={busy} onClick={()=>void send('bookmark',signedIn&&value.bookmarked?'DELETE':'PUT')}>{signedIn&&value.bookmarked?'Remove bookmark':'Bookmark'}</button></div>
      {!signedIn&&<p>Sign in to comment or review.</p>}
      <div className="field-pair">
        <FeedbackList label="Comments" entries={value.comments} signedIn={signedIn} busy={busy} submit={body=>send('comments','POST',{body})}/>
        <FeedbackList label="Reviews" entries={value.reviews} signedIn={signedIn} busy={busy} submit={body=>send('reviews','POST',{body})}/>
      </div>
    </>:!error?<p role="status">Loading community activity…</p>:<button className="button outline small" onClick={()=>{setError('');setRevision(x=>x+1);}}>Retry</button>}
    <ErrorText text={error}/>
  </section>;
}
