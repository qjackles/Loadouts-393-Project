import React, { useEffect, useState, type FormEvent } from 'react';
import { createRoot } from 'react-dom/client';
import { ArrowLeft, Layers, LogOut, Plus, Search, X } from 'lucide-react';
import { api, ApiError, byId, categories, message, type Loadout, type ProductFields, type Profile, type User } from './api';
import { Cards, CommunityPanel, emptyProduct, ErrorText, Price, ProductFieldsEditor, Products, useData } from './parts';
import './style.css';

type View = {page:string; id:string};
const currentView = ():View => {
  const [page='',id='']=location.hash.slice(1).split('/');
  return {page,id};
};
const profilePath = (id:string) => '/profiles/'+encodeURIComponent(id);
const optionCategories = categories.map(c=><option key={c}>{c}</option>);

function AuthDialog({ initial, close, done }: {
  initial:'login'|'register'; close:()=>void; done:(user:User)=>void;
}) {
  const [mode,setMode]=useState(initial);
  const [busy,setBusy]=useState(false);
  const [error,setError]=useState('');
  const [dialog,setDialog]=useState<HTMLDialogElement|null>(null);
  useEffect(()=>{ if(dialog && !dialog.open) dialog.showModal(); },[dialog]);
  async function submit(e:FormEvent<HTMLFormElement>){
    e.preventDefault(); const f=new FormData(e.currentTarget);setError('');setBusy(true);
    try {done(await api<User>('/auth/'+mode,'POST',Object.fromEntries(f)));}
    catch(e){setError(message(e));}finally{setBusy(false);}
  }
  return <dialog ref={setDialog} className="modal auth-dialog" onCancel={e=>{e.preventDefault();if(!busy)close();}}>
    <button type="button" aria-label="Close" className="modal-close icon-button" onClick={close} disabled={busy}><X size={20}/></button>
    <h2>{mode==='login'?'Sign in':'Create an account'}</h2>
    <form key={mode} onSubmit={submit}>
      {mode==='register'&&<label>Username<input name="username" autoComplete="username" required maxLength={255}/></label>}
      <label>Email<input name="email" type="email" autoComplete="email" required maxLength={255}/></label>
      <label>Password<input name="password" type="password" autoComplete={mode==='register'?'new-password':'current-password'} required minLength={mode==='register'?8:undefined} maxLength={72}/></label>
      <ErrorText text={error}/><button className="button dark full" disabled={busy}>{busy?'Working…':mode==='login'?'Sign in':'Create account'}</button>
    </form>
    <p><button type="button" className="text-button" disabled={busy} onClick={()=>{setError('');setMode(mode==='login'?'register':'login');}}>{mode==='login'?'Create an account':'Back to sign in'}</button></p>
  </dialog>;
}
function Discover({ revision, create }: { revision:number; create:()=>void }) {
  const [query,setQuery]=useState('');
  const [search,setSearch]=useState('');
  const [category,setCategory]=useState('');
  const [sort,setSort]=useState('newest');
  const [range,setRange]=useState({min:'',max:''});
  const [min,setMin]=useState('');
  const [max,setMax]=useState('');
  const [retry,setRetry]=useState(0);
  const [filterError,setFilterError]=useState('');
  const params=new URLSearchParams({q:search,category,sort});
  if(range.min) params.set('minPrice',range.min);
  if(range.max) params.set('maxPrice',range.max);
  const results=useData<Loadout[]>('/loadouts?'+params,revision+retry);
  function apply(e:FormEvent<HTMLFormElement>){
    e.preventDefault();
    if(min&&max&&Number(min)>Number(max)){setFilterError('Minimum price must be less than or equal to maximum price.');return;}
    setFilterError('');setSearch(query);setRange({min,max});
  }
  function reset(){setQuery('');setSearch('');setCategory('');setSort('newest');setMin('');setMax('');setRange({min:'',max:''});setFilterError('');}
  return <>
    <section className="hero"><div className="hero-copy"><div className="eyebrow"><span/> BUILT AROUND WHAT YOU LOVE</div>
      <h1>Discover your next thing<span>.</span></h1><p>Find ideas for your next hobby and share the gear you use.</p>
      <button className="button dark" onClick={create}><Plus size={18}/> Create a loadout</button>
    </div><div className="hero-art" aria-hidden="true"><div className="orbit orbit-one"/><div className="orbit orbit-two"/><Layers size={112} strokeWidth={.75}/></div></section>
    <section className="collections"><h2>Discover loadouts</h2>
      <form className="discover-form" role="search" onSubmit={apply}>
        <div className="discover-row"><label><span><Search size={14}/> Search</span><input value={query} maxLength={200} onChange={e=>setQuery(e.target.value)} placeholder="Try baking or camping"/></label>
        <label>Sort by<select value={sort} onChange={e=>setSort(e.target.value)}><option value="newest">Newest first</option><option value="oldest">Oldest first</option><option value="title">Title A–Z</option><option value="price-low">Price low to high</option><option value="price-high">Price high to low</option></select></label></div>
        <div className="discover-row"><label>Minimum known price<input type="number" min="0" step=".01" value={min} onChange={e=>setMin(e.target.value)}/></label>
        <label>Maximum known price<input type="number" min="0" step=".01" value={max} onChange={e=>setMax(e.target.value)}/></label>
        <div className="action-row"><button className="button dark small">Search and apply</button><button type="button" className="button outline small" onClick={reset}>Clear filters</button></div></div>
      </form>
      <div className="categories">{['',...categories].map(c=><button key={c} type="button" aria-pressed={c===category} className={c===category?'selected':''} onClick={()=>setCategory(c)}>{c||'All collections'}</button>)}</div>
      <ErrorText text={filterError}/><ErrorText text={results.error}/>
      {results.loading?<p role="status">Loading collections…</p>:results.error?<button className="button outline" onClick={()=>setRetry(x=>x+1)}>Try again</button>
        :results.data&&<Cards items={results.data} empty="No collections match your search."/>}
    </section>
  </>;
}
function Listing({ page, revision }: {page:'mine'|'bookmarks'; revision:number}) {
  const {data,error,loading}=useData<Loadout[]>('/loadouts/'+page,revision);
  return <section className="page"><h1>{page==='mine'?'My loadouts':'Bookmarks'}</h1><ErrorText text={error}/>
    {loading?<p role="status">Loading…</p>:data&&<Cards items={data} empty={page==='mine'?'You have no saved loadouts.':'You have no bookmarks yet.'}/>}</section>;
}
function Editor({ existing, changed }: { existing?:Loadout; changed:()=>void }) {
  const [products,setProducts]=useState<{key:number; fields:ProductFields}[]>([]);
  const counter=React.useRef(0);
  const [error,setError]=useState('');
  const [busy,setBusy]=useState(false);
  async function submit(e:FormEvent<HTMLFormElement>){
    e.preventDefault(); const fields = new FormData(e.currentTarget);
    const submitter=(e.nativeEvent as SubmitEvent).submitter as HTMLButtonElement|null;
    const draft=submitter?.value === 'draft';
    const input={...Object.fromEntries(fields),draft,...(!existing?{products:products.map(p=>p.fields)}:{})};
    setError('');setBusy(true);
    try{
      const result=await api<Loadout>(existing?byId(existing.id):'/loadouts',existing?'PUT':'POST',input);
      changed();location.hash='loadout/'+result.id;
    }catch(e){setError(message(e));}finally{setBusy(false);}
  }
  return <section className="page editor"><a href={existing?'#loadout/'+existing.id:'#mine'} className="back"><ArrowLeft size={16}/> Back</a>
    <h1>{existing?'Edit loadout':'Create a loadout'}</h1>
    <form onSubmit={submit} className="editor-form">
      <label>Collection title<input name="title" required maxLength={120} defaultValue={existing?.title}/></label>
      <label>Category<select name="category" defaultValue={existing?.category||''}><option value="">Choose a category (optional)</option>
        {existing?.category&&!categories.includes(existing.category)&&<option>{existing.category}</option>}{optionCategories}</select></label>
      <label>Description<textarea name="description" maxLength={5000} rows={4} defaultValue={existing?.description||''}/></label>
      {!existing&&<section><h2>Products</h2><p>Product links, prices, and availability can be left blank.</p>
        {products.map((p,i)=><fieldset className="product-fields" key={p.key}><legend>Product {i+1}</legend>
          <ProductFieldsEditor value={p.fields} change={fields=>setProducts(old=>old.map(x=>x.key===p.key?{...x,fields}:x))}/>
          <button type="button" className="button outline small" onClick={()=>setProducts(old=>old.filter(x=>x.key!==p.key))}>Remove product {i+1}</button></fieldset>)}
        <button type="button" disabled={products.length>=50||busy} className="button outline small" onClick={()=>setProducts(old=>[...old,{key:counter.current++,fields:emptyProduct}])}>Add a product</button>
      </section>}
      <p className="fine">Drafts are visible only to you. A private profile also hides your published loadouts from other users.</p>
      <ErrorText text={error}/><div className="form-footer">
        <button disabled={busy} className="button outline" type="submit" value="draft">Save draft</button>
        <button disabled={busy} className="button dark" type="submit" value="publish">{existing&&!existing.draft?'Save and keep published':'Publish loadout'}</button>
      </div>
    </form>
  </section>;
}
function EditPage({ id,user,changed }: {id:string;user:User;changed:()=>void}){
  const {data,loading,error}=useData<Loadout>(byId(id));
  if(loading)return <p className="page" role="status">Loading loadout…</p>;
  if(!data||data.ownerId!==user.id)return <section className="page"><ErrorText text={error||'Only the creator can edit this loadout.'}/></section>;
  return <Editor key={id} existing={data} changed={changed}/>;
}
function Details({ id,user,revision,changed,signIn }: {id:string;user:User|null;revision:number;changed:()=>void;signIn:()=>void}) {
  const [detailRevision,refresh]=useState(0);
  const {data,loading,error}=useData<Loadout>(byId(id),revision);
  const [summary,setSummary]=useState<Loadout|null>(null);
  const suggestions=useData<Loadout[]>(byId(id)+'/suggestions',revision+detailRevision);
  const [removeError,setRemoveError]=useState('');
  const [busy,setBusy]=useState(false);
  useEffect(()=>{if(data)setSummary(data);},[data]);
  async function productChanged(){
    refresh(x=>x+1);
    try{setSummary(await api<Loadout>(byId(id)));}catch(e){setRemoveError(message(e));}
  }
  async function remove(){
    if(!confirm('Delete this loadout and its products?'))return;
    setRemoveError('');setBusy(true);
    try{await api(byId(id),'DELETE');changed();location.hash='mine';}
    catch(e){setRemoveError(message(e));}finally{setBusy(false);}
  }
  const owner=!!user && data?.ownerId===user.id;
  return <section className="page detail"><a className="back" href="#"><ArrowLeft size={16}/> Back to Discover</a>
    {loading?<p role="status">Loading loadout…</p>:!data?<ErrorText text={error||'This loadout is unavailable.'}/>:<>
      <div className="eyebrow">{data.draft?'PRIVATE DRAFT':data.category||'COLLECTION'}</div>
      <h1>{data.title}</h1><div className="detail-author">Created by <a href={'#profile/'+data.ownerId}>@{data.ownerUsername}</a></div>
      <p className="description">{data.description||'No description provided.'}</p>
      <Price loadout={summary??data}/>
      <p className="fine">Stored USD prices. Tax and shipping are excluded.</p>
      {owner&&<div className="action-row"><a className="button outline small" href={'#edit/'+id}>Edit loadout</a>
        <button type="button" className="button outline small" disabled={busy} onClick={()=>void remove()}>Delete loadout</button></div>}
      <ErrorText text={removeError}/>
      <Products id={id} owner={owner} revision={revision+detailRevision} changed={()=>void productChanged()}/>
      {data.publiclyVisible&&<CommunityPanel id={id} signedIn={!!user} signIn={signIn} authRevision={user?.id||''}/>}
      {!!suggestions.data?.length&&<section className="products"><h2>Related loadouts</h2><Cards items={suggestions.data} empty=""/></section>}
    </>}
  </section>;
}
function ProfilePage({id,user,revision,changed}:{id:string;user:User|null;revision:number;changed:()=>void}){
  const {data,loading,error}=useData<Profile>(profilePath(id),revision);
  return <section className="page"><a className="back" href="#"><ArrowLeft size={16}/> Back to Discover</a>
    {loading?<p role="status">Loading profile…</p>:!data?<ErrorText text={error||'This profile is unavailable.'}/>:<>
      <h1>{data.displayName||'@'+data.username}</h1>
      {data.displayName&&<p>@{data.username}</p>}
      <p className="description">{data.bio||'No bio yet.'}</p>
      {data.privateProfile&&<p>This profile is private.</p>}
      {user?.id===id&&<ProfileForm profile={data} changed={changed}/>}
      <section className="products"><h2>Published loadouts</h2><Cards items={data.loadouts} empty="No published loadouts yet."/></section>
    </>}
  </section>;
}
function ProfileForm({profile,changed}:{profile:Profile;changed:()=>void}){
  const [bio,setBio]=useState(profile.bio??'');
  const [displayName,setDisplayName]=useState(profile.displayName??'');
  const [privateProfile,setPrivate]=useState(profile.privateProfile);
  const [error,setError]=useState('');
  const [busy,setBusy]=useState(false);
  async function save(e:FormEvent){
    e.preventDefault();setError('');setBusy(true);
    try{await api('/profiles/me','PUT',{displayName,bio,privateProfile});changed();}
    catch(e){setError(message(e));}finally{setBusy(false);}
  }
  return <form className="product-form" onSubmit={save}><h2>Edit your profile</h2>
    <label>Display name<input maxLength={80} value={displayName} onChange={e=>setDisplayName(e.target.value)}/></label>
    <label>Bio<textarea rows={3} maxLength={1000} value={bio} onChange={e=>setBio(e.target.value)}/></label>
    <label className="check-label"><input type="checkbox" checked={privateProfile} onChange={e=>setPrivate(e.target.checked)}/> Private profile</label>
    <p>When private, only you can see your profile and loadouts.</p>
    <button className="button dark" disabled={busy}>Save profile</button><ErrorText text={error}/>
  </form>;
}
function App(){
  const [view,setView]=useState(currentView);
  const [user,setUser]=useState<User|null>(null);
  const [checking,setChecking]=useState(true);
  const [auth,setAuth]=useState<'login'|'register'|null>(null);
  const [afterAuth,setAfterAuth]=useState('');
  const [notice,setNotice]=useState('');
  const [revision,setRevision]=useState(0);
  const [loggingOut,setLoggingOut]=useState(false);
  const changed=()=>setRevision(n=>n+1);
  useEffect(()=>{
    const hash=()=>{setView(currentView());window.scrollTo(0,0);};
    const controller=new AbortController();
    window.addEventListener('hashchange',hash);
    api<User>('/auth/me','GET',undefined,controller.signal).then(u=>{if(!controller.signal.aborted)setUser(u);}).catch(e=>{
      if(!controller.signal.aborted&&(!(e instanceof ApiError)||e.status!==401))setNotice(message(e));
    }).finally(()=>{if(!controller.signal.aborted)setChecking(false);});
    return ()=>{window.removeEventListener('hashchange',hash);controller.abort();};
  },[]);
  function request(next=''){
    setAfterAuth(next);setAuth('login');
  }
  const create=()=>user?location.hash='create':request('create');
  async function logout(){
    setLoggingOut(true);
    try{await api('/auth/logout','POST');setUser(null);changed();location.hash='';}
    catch(e){setNotice(message(e));}finally{setLoggingOut(false);}
  }
  const protectedView=['mine','bookmarks','create','edit'].includes(view.page);
  let content:React.ReactNode;
  if(checking&&protectedView)content=<p className="page" role="status">Checking your session…</p>;
  else if(protectedView&&!user) content=<section className="page"><h1>Sign in to continue</h1><button className="button dark" onClick={()=>request()}>Sign in</button></section>;
  else if(view.page==='create') content=<Editor key="new" changed={changed}/>;
  else if(view.page==='edit'&&user) content=<EditPage key={view.id} id={view.id} user={user} changed={changed}/>;
  else if(view.page==='mine'||view.page==='bookmarks') content=<Listing key={view.page} page={view.page} revision={revision}/>;
  else if(view.page==='loadout'&&view.id) content=<Details key={view.id} id={view.id} user={user} revision={revision} changed={changed} signIn={()=>request()}/>;
  else if(view.page==='profile'&&view.id) content=<ProfilePage key={view.id} id={view.id} user={user} revision={revision} changed={changed}/>;
  else content=<Discover revision={revision} create={create}/>;
  return <><header className="header"><a className="brand" href="#" aria-label="Loadouts home"><span className="brand-icon"><Layers size={21}/></span>loadouts<span className="brand-dot">.</span></a>
    <nav aria-label="Main navigation"><a href="#">Discover</a>
      {user?<><a href="#mine">My loadouts</a><a href="#bookmarks">Bookmarks</a><a href={'#profile/'+user.id} className="username" aria-label="My profile">@{user.username}</a>
        <button type="button" className="icon-button" disabled={loggingOut} aria-label="Sign out" onClick={()=>void logout()}><LogOut size={19}/></button></>
        :<button type="button" className="text-button" disabled={checking} onClick={()=>request()}>Sign in</button>}
      <button type="button" className="button dark small" disabled={checking} onClick={create}><Plus size={16}/> Create loadout</button></nav></header>
    {notice&&<div className="notice" role="status">{notice}<button className="icon-button" onClick={()=>setNotice('')} aria-label="Dismiss"><X size={16}/></button></div>}
    <main>{content}</main><footer><a className="brand" href="#">loadouts.</a><span>Collections for the things you love.</span></footer>
    {auth&&<AuthDialog initial={auth} close={()=>setAuth(null)} done={u=>{setUser(u);setAuth(null);if(afterAuth)location.hash=afterAuth;changed();}}/>}
  </>;
}
createRoot(document.getElementById('root')!).render(<React.StrictMode><App/></React.StrictMode>);

