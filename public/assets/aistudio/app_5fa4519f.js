'use strict';
/* Skywall வாடிக்கையாளர்கள் - standalone app. Data lives in this browser (localStorage). */
(function () {
  var KEY = 'skywall.customers.v1', SKEY = 'skywall.settings.v1';
  var STAGES = [
    ['enquiry', 'விசாரணை'], ['quote', 'மதிப்பீடு அனுப்பப்பட்டது'], ['confirmed', 'உறுதியானது'],
    ['progress', 'வேலை நடக்கிறது'], ['done', 'முடிந்தது']
  ];
  var JOBS = ['உள் சுவர் பெயிண்டிங்', 'வெளிப்புற பெயிண்டிங்', 'டெக்ஸ்சர் / டிசைன்', 'வாட்டர் ப்ரூஃபிங்', 'மர வேலை பாலிஷ்', 'மற்றவை'];
  var MON = ['ஜன.', 'பிப்.', 'மார்.', 'ஏப்.', 'மே', 'ஜூன்', 'ஜூலை', 'ஆக.', 'செப்.', 'அக்.', 'நவ.', 'டிச.'];
  var MONF = ['ஜனவரி', 'பிப்ரவரி', 'மார்ச்', 'ஏப்ரல்', 'மே', 'ஜூன்', 'ஜூலை', 'ஆகஸ்ட்', 'செப்டம்பர்', 'அக்டோபர்', 'நவம்பர்', 'டிசம்பர்'];
  var DEF = {
    name: 'ஸ்கைவால் பெயிண்டிங் சொல்யூஷன்ஸ்',
    address: 'எண் 5, பொன்னியம்மன் கோவில் பின்புறம், பட்டறை கிராமம், அத்திகத்தூர் ரோடு, மணவாளநகர் 602002',
    phone: '6380817101', theme: 'auto'
  };

  var $ = function (s, r) { return (r || document).querySelector(s); };
  var esc = function (s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]; }); };
  var inr = function (n) { return '₹' + Math.round(n || 0).toLocaleString('en-IN'); };
  var uid = function () { return 'c' + Date.now().toString(36) + Math.random().toString(36).slice(2, 6); };
  var stageName = function (k) { var s = STAGES.filter(function (x) { return x[0] === k; })[0]; return s ? s[1] : k; };

  /* ---------- dates ---------- */
  function ymd(d) { return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); }
  function today() { return ymd(new Date()); }
  function addDays(n) { var d = new Date(); d.setDate(d.getDate() + n); return ymd(d); }
  function parse(s) { var p = String(s).split('-'); return new Date(+p[0], +p[1] - 1, +p[2]); }
  function short(s) { if (!s) return ''; var d = parse(s); return MON[d.getMonth()] + ' ' + d.getDate(); }
  function diffDays(s) { var a = parse(s), b = parse(today()); return Math.round((b - a) / 864e5); }

  /* ---------- storage ---------- */
  var customers = [], settings = Object.assign({}, DEF), filter = 'all';
  function load() {
    try { customers = JSON.parse(localStorage.getItem(KEY)) || null; } catch (e) { customers = null; }
    try { settings = Object.assign({}, DEF, JSON.parse(localStorage.getItem(SKEY)) || {}); } catch (e) { settings = Object.assign({}, DEF); }
    if (!customers) { customers = sample(); save(); }
  }
  function save() {
    try { localStorage.setItem(KEY, JSON.stringify(customers)); } catch (e) { toast('சேமிக்க முடியவில்லை (உலாவி சேமிப்பு தடை)'); }
  }
  function saveSettings() { try { localStorage.setItem(SKEY, JSON.stringify(settings)); } catch (e) {} }

  function sample() {
    var mk = function (id, name, phone, place, stage, jobs, paid, visit, follow) {
      return {
        id: id, sample: true, name: name + ' (மாதிரி)', phone: phone, place: place, stage: stage, jobs: jobs,
        discount: 0, visit: visit || '', follow: follow || '', notes: '', created: Date.now() - Math.random() * 1e8,
        payments: paid ? [{ id: uid(), date: today(), amt: paid, note: 'முன்பணம்' }] : []
      };
    };
    return [
      mk('ex1', 'முருகன்', '9000000001', 'மணவாளநகர்', 'progress', [
        { type: 'உள் சுவர் பெயிண்டிங்', colour: 'ஐவரி', hex: '#e8dcc8', area: 1900, rate: 12 },
        { type: 'வெளிப்புற பெயிண்டிங்', colour: 'க்ரீம்', hex: '#d8d2c4', area: 1000, rate: 23 }], 15000, addDays(-4), addDays(1)),
      mk('ex2', 'லதா', '9000000002', 'திருவள்ளூர்', 'quote', [
        { type: 'வெளிப்புற பெயிண்டிங்', colour: 'டெரகோட்டா 4412', hex: '#d9694a', area: 2400, rate: 18 }], 0, addDays(-3), addDays(-1)),
      mk('ex3', 'ரமேஷ்', '9000000003', 'கடம்பத்தூர்', 'done', [
        { type: 'வாட்டர் ப்ரூஃபிங்', colour: '', hex: '#7fae8a', area: 900, rate: 22 }], 19800, addDays(-60)),
      mk('ex4', 'செல்வி', '9000000004', 'பூந்தமல்லி', 'confirmed', [
        { type: 'டெக்ஸ்சர் / டிசைன்', colour: 'ஸ்கை ப்ளூ 2207', hex: '#0a6f93', area: 600, rate: 35 }], 5000, addDays(-1), ''),
      mk('ex5', 'கார்த்திக்', '9000000005', 'மணவாளநகர்', 'enquiry', [
        { type: 'மர வேலை பாலிஷ்', colour: '', hex: '#8a5a2b', area: 1, rate: 14000 }], 0, '', addDays(3))
    ];
  }

  /* ---------- calculations ---------- */
  function lineAmt(j) { return (+j.area || 0) * (+j.rate || 0); }
  function sub(c) { return c.jobs.reduce(function (a, j) { return a + lineAmt(j); }, 0); }
  function total(c) { return Math.max(0, sub(c) - (+c.discount || 0)); }
  function paid(c) { return c.payments.reduce(function (a, p) { return a + (+p.amt || 0); }, 0); }
  function due(c) { return Math.max(0, total(c) - paid(c)); }
  function jobTitle(c) {
    var t = c.jobs.map(function (j) { return j.type; }).filter(Boolean);
    if (!t.length) return 'வேலை குறிப்பிடப்படவில்லை';
    return t[0] + (t.length > 1 ? ' +' + (t.length - 1) : '');
  }
  function followLate(c) { return c.follow && c.stage !== 'done' && diffDays(c.follow) >= 1; }

  /* ---------- WhatsApp ---------- */
  function sig() { return '\n\n' + settings.name + '\n' + settings.address + '\nநன்றி.'; }
  function message(c) {
    var hi = 'வணக்கம் ' + c.name + ',\n';
    if (c.stage === 'enquiry') return hi + 'பெயிண்டிங் வேலைக்கான தள ஆய்வுக்கு வர விரும்புகிறோம். உங்களுக்கு வசதியான தேதி மற்றும் நேரத்தை தெரிவிக்கவும்.' + sig();
    if (c.stage === 'progress' || (c.stage === 'done' && due(c) > 0)) {
      if (due(c) > 0) return hi + 'உங்கள் ' + c.jobs.map(function (j) { return j.type; }).filter(Boolean).join(', ') + ' வேலைக்கான பாக்கி தொகை ' + inr(due(c)) + ' (இறுதி மதிப்பீடு ' + inr(total(c)) + ', பெற்றது ' + inr(paid(c)) + '). வசதியான நேரத்தில் செலுத்துமாறு கேட்டுக்கொள்கிறோம்.' + sig();
    }
    if (c.stage === 'done') return hi + 'உங்கள் பெயிண்டிங் வேலை நல்ல முறையில் முடிவடைந்தது. எங்களைத் தேர்ந்தெடுத்ததற்கு நன்றி. வேலையில் ஏதேனும் குறை இருந்தால் தயங்காமல் தெரிவிக்கவும்.' + sig();
    var lines = c.jobs.map(function (j, i) {
      return (i + 1) + ') ' + j.type + (j.colour ? ' (' + j.colour + ')' : '') + ' – ' + j.area + ' ச.அடி × ₹' + j.rate + ' = ' + inr(lineAmt(j));
    }).join('\n');
    var m = hi + settings.name + ' சார்பாக உங்கள் பெயிண்டிங் வேலைக்கான மதிப்பீடு:\n\n' + lines + '\n\nமொத்தம்: ' + inr(sub(c));
    if (+c.discount) m += '\nதள்ளுபடி: ' + inr(c.discount);
    m += '\nஇறுதி மதிப்பீடு: ' + inr(total(c));
    if (paid(c)) m += '\nபெற்ற தொகை: ' + inr(paid(c));
    m += '\nபாக்கி: ' + inr(due(c));
    return m + sig();
  }
  function waLink(c, text) {
    var p = String(c.phone || '').replace(/\D/g, '');
    if (p.length === 10) p = '91' + p;
    return 'https://wa.me/' + p + '?text=' + encodeURIComponent(text);
  }

  /* ---------- dashboard ---------- */
  function render() {
    var biz = $('#bizInfo');
    biz.innerHTML = '<b>' + esc(settings.name) + '</b><br>' + esc(settings.address) + (settings.phone ? ' · ' + esc(settings.phone) : '');
    applyTheme();
    var hasSample = customers.some(function (c) { return c.sample; });
    $('#exampleNote').hidden = !hasSample;

    // tasks
    var t = [];
    customers.forEach(function (c) {
      if (c.stage !== 'done' && c.follow) {
        var d = diffDays(c.follow);
        if (d >= 0) t.push({ c: c, late: d > 0, txt: 'பின்தொடர்தல் · ' + (d > 0 ? d + ' நாள் தாமதம்' : 'இன்று') });
      }
      if (c.visit === today()) t.push({ c: c, late: false, txt: 'தள ஆய்வு · இன்று' });
    });
    var ts = $('#tasks');
    ts.hidden = !t.length;
    ts.innerHTML = t.length ? '<h2 id="tasksH">இன்றைய பணிகள் (' + t.length + ')</h2><div class="tlist">' + t.map(function (x) {
      return '<button type="button" class="task' + (x.late ? ' late' : '') + '" data-open="' + x.c.id + '"><b>' + esc(x.c.name) + '</b><span>' + x.txt + '</span></button>';
    }).join('') + '</div>' : '';

    // stats
    var m = today().slice(0, 7);
    var newCnt = customers.filter(function (c) { return c.stage === 'enquiry' || c.stage === 'quote'; }).length;
    var active = customers.filter(function (c) { return c.stage === 'confirmed' || c.stage === 'progress'; }).reduce(function (a, c) { return a + total(c); }, 0);
    var month = 0;
    customers.forEach(function (c) { c.payments.forEach(function (p) { if (String(p.date).slice(0, 7) === m) month += +p.amt || 0; }); });
    var dueAll = customers.filter(inDue).reduce(function (a, c) { return a + due(c); }, 0);
    $('#stats').innerHTML = [
      ['மொத்த வாடிக்கையாளர்கள்', customers.length], ['புதிய விசாரணை / மதிப்பீடு', newCnt],
      ['நடப்பு வேலை மதிப்பு', inr(active)], ['இந்த மாதம் பெற்றது', inr(month)], ['மொத்த பாக்கி', inr(dueAll)]
    ].map(function (s) { return '<div class="stat"><span>' + s[0] + '</span><b>' + s[1] + '</b></div>'; }).join('');

    // chart
    var months = [], now = new Date();
    for (var i = 5; i >= 0; i--) { var d = new Date(now.getFullYear(), now.getMonth() - i, 1); months.push({ k: d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0'), y: d.getFullYear(), m: d.getMonth(), v: 0 }); }
    customers.forEach(function (c) { c.payments.forEach(function (p) { var k = String(p.date).slice(0, 7); months.forEach(function (x) { if (x.k === k) x.v += +p.amt || 0; }); }); });
    var mx = Math.max.apply(null, months.map(function (x) { return x.v; })) || 1;
    var kfmt = function (v) { return v ? (v >= 1000 ? '₹' + +(v / 1000).toFixed(1) + 'K' : '₹' + v) : '–'; };
    $('#chart').setAttribute('role', 'img');
    $('#chart').setAttribute('aria-label', 'மாத வாரியாகப் பெற்ற தொகை: ' + months.map(function (x) { return MONF[x.m] + ' ' + x.y + ' ' + inr(x.v); }).join(', '));
    $('#chart').innerHTML = months.map(function (x, i) {
      return '<div class="cb" title="' + MONF[x.m] + ' ' + x.y + ': ' + inr(x.v) + '"><span class="cv">' + kfmt(x.v) + '</span><div class="trk"><i style="height:' + (x.v / mx * 100) + '%;opacity:' + (i === 5 ? 1 : .6) + '"></i></div><span class="cl">' + MON[x.m] + '</span></div>';
    }).join('');

    // dues
    var dl = customers.filter(inDue).filter(function (c) { return due(c) > 0; }).sort(function (a, b) { return due(b) - due(a); });
    $('#dues').innerHTML = dl.length ? dl.map(function (c) {
      return '<li data-open="' + c.id + '"><span class="n">' + esc(c.name) + '<br><span class="meta">' + stageName(c.stage) + '</span></span><b>' + inr(due(c)) + '</b></li>';
    }).join('') : '<li class="empty-s">பாக்கி எதுவும் இல்லை</li>';

    // stage bar + filters
    var cnt = {}; STAGES.forEach(function (s) { cnt[s[0]] = customers.filter(function (c) { return c.stage === s[0]; }).length; });
    var tot = customers.length || 1;
    $('#bar').innerHTML = STAGES.map(function (s) { return '<i style="width:' + cnt[s[0]] / tot * 100 + '%;background:var(--s-' + s[0] + ')"></i>'; }).join('');
    var fu = customers.filter(function (c) { return c.stage !== 'done' && c.follow && diffDays(c.follow) >= 0; }).length;
    $('#filters').innerHTML = '<button type="button" class="f" data-filter="all" aria-pressed="' + (filter === 'all') + '">அனைத்தும் <b>' + customers.length + '</b></button>' +
      STAGES.map(function (s) { return '<button type="button" class="f" data-filter="' + s[0] + '" aria-pressed="' + (filter === s[0]) + '"><span class="dot" style="background:var(--s-' + s[0] + ')"></span>' + s[1] + ' <b>' + cnt[s[0]] + '</b></button>'; }).join('') +
      '<button type="button" class="f" data-filter="followup" aria-pressed="' + (filter === 'followup') + '">பின்தொடர்தல் <b>' + fu + '</b></button>';

    renderList();
  }
  function inDue(c) { return c.stage === 'confirmed' || c.stage === 'progress' || c.stage === 'done'; }

  function renderList() {
    var q = $('#search').value.trim().toLowerCase(), sort = $('#sort').value;
    var l = customers.filter(function (c) {
      if (filter === 'followup') { if (!(c.stage !== 'done' && c.follow && diffDays(c.follow) >= 0)) return false; }
      else if (filter !== 'all' && c.stage !== filter) return false;
      if (!q) return true;
      var hay = [c.name, c.phone, c.place, c.notes].concat(c.jobs.map(function (j) { return j.type + ' ' + j.colour; })).join(' ').toLowerCase();
      return hay.indexOf(q) > -1;
    });
    l.sort(function (a, b) {
      if (sort === 'due') return due(b) - due(a);
      if (sort === 'value') return total(b) - total(a);
      if (sort === 'follow') return (a.follow || '9999').localeCompare(b.follow || '9999');
      if (sort === 'name') return a.name.localeCompare(b.name, 'ta');
      return b.created - a.created;
    });
    $('#list').innerHTML = l.length ? l.map(card).join('') :
      '<div class="empty"><b>வாடிக்கையாளர்கள் இல்லை</b>' + (customers.length ? 'வடிகட்டியை மாற்றிப் பாருங்கள்.' : '"+ புதிய வாடிக்கையாளர்" பொத்தானை அழுத்தவும்.') + '</div>';
  }

  function card(c) {
    var pct = total(c) ? Math.min(100, paid(c) / total(c) * 100) : 0;
    var sws = c.jobs.filter(function (j) { return j.hex; }).map(function (j) { return '<span class="sw" style="background:' + esc(j.hex) + '"></span>'; }).join('');
    var badge = '';
    if (c.stage !== 'done' && c.follow) {
      var d = diffDays(c.follow);
      badge = d > 0 ? '<span class="badge late">பின்தொடர்தல் ' + d + ' நாள் தாமதம்</span>' : '<span class="badge">பின்தொடர்தல் ' + (d === 0 ? 'இன்று' : short(c.follow)) + '</span>';
    }
    return '<article class="card" data-open="' + c.id + '" tabindex="0"><header><div style="min-width:0"><h3>' + esc(c.name) + '</h3>' +
      '<div class="meta"><span class="phone">' + esc(c.phone) + '</span>' + (c.place ? ' · ' + esc(c.place) : '') + (c.visit ? ' · தள ஆய்வு ' + short(c.visit) : '') + '</div></div>' +
      '<span class="pill" style="--c:var(--s-' + c.stage + ')">' + stageName(c.stage) + '</span></header>' +
      '<div class="job"><span class="sws">' + sws + '</span><span class="t">' + esc(jobTitle(c)) + '</span></div>' +
      '<dl class="nums"><div><dt>மதிப்பீடு</dt><dd>' + inr(total(c)) + '</dd></div><div><dt>பெற்றது</dt><dd>' + inr(paid(c)) + '</dd></div><div><dt>பாக்கி</dt><dd>' + inr(due(c)) + '</dd></div></dl>' +
      '<div class="track"><i style="width:' + pct + '%"></i></div>' + badge +
      '<footer><select data-stage-of="' + c.id + '" aria-label="நிலை மாற்று">' + stageOpts(c.stage) + '</select>' +
      '<a class="btn small" href="' + waLink(c, message(c)) + '" target="_blank" rel="noopener">WhatsApp</a>' +
      '<button class="btn small" type="button" data-act="detail" data-id="' + c.id + '">விவரம்</button></footer></article>';
  }
  function stageOpts(cur) { return STAGES.map(function (s) { return '<option value="' + s[0] + '"' + (s[0] === cur ? ' selected' : '') + '>' + s[1] + '</option>'; }).join(''); }

  /* ---------- drawer ---------- */
  function drawer(title, body, foot) {
    $('#overlay').innerHTML = '<div class="scrim" data-act="close"></div><aside class="drawer" role="dialog" aria-modal="true" aria-label="' + esc(title) + '">' +
      '<div class="d-head"><h2>' + esc(title) + '</h2><button class="btn small" type="button" data-act="close">மூடு</button></div>' +
      '<div class="d-body">' + body + '</div>' + (foot ? '<div class="d-foot">' + foot + '</div>' : '') + '</aside>';
    document.body.classList.add('lock');
  }
  function closeDrawer() { $('#overlay').innerHTML = ''; document.body.classList.remove('lock'); }

  function detail(id) {
    var c = get(id); if (!c) return;
    var items = c.jobs.map(function (j) {
      return '<tr><td>' + (j.hex ? '<span class="sw big" style="background:' + esc(j.hex) + ';display:inline-block;vertical-align:middle"></span> ' : '') + esc(j.type) + (j.colour ? '<br><span class="meta">' + esc(j.colour) + '</span>' : '') + '</td><td class="r">' + (+j.area || 0) + '</td><td class="r">₹' + (+j.rate || 0) + '</td><td class="r">' + inr(lineAmt(j)) + '</td></tr>';
    }).join('');
    var pays = c.payments.length ? c.payments.slice().sort(function (a, b) { return b.date.localeCompare(a.date); }).map(function (p) {
      return '<div class="pay"><span>' + short(p.date) + (p.note ? ' · <span class="meta">' + esc(p.note) + '</span>' : '') + '</span><span><span class="a">' + inr(p.amt) + '</span> <button class="btn small danger" type="button" data-act="delpay" data-id="' + c.id + '" data-pid="' + p.id + '" aria-label="நீக்கு">✕</button></span></div>';
    }).join('') : '<div class="empty-s">இன்னும் தொகை பெறவில்லை</div>';
    var body =
      '<div class="sec"><dl class="kv"><dt>நிலை</dt><dd><select class="sel" data-stage-of="' + c.id + '">' + stageOpts(c.stage) + '</select></dd>' +
      '<dt>தொலைபேசி</dt><dd><a href="tel:' + esc(c.phone) + '">' + esc(c.phone) + '</a></dd>' +
      '<dt>இடம்</dt><dd>' + esc(c.place || '–') + '</dd>' +
      '<dt>தள ஆய்வு</dt><dd>' + (c.visit ? short(c.visit) : '–') + '</dd>' +
      '<dt>பின்தொடர்தல்</dt><dd>' + (c.follow ? short(c.follow) : '–') + '</dd>' +
      (c.notes ? '<dt>குறிப்பு</dt><dd>' + esc(c.notes) + '</dd>' : '') + '</dl></div>' +
      '<div class="sec"><h3>வேலை விவரம்</h3><div class="tblw"><table><thead><tr><th>வேலை</th><th class="r">ச.அடி</th><th class="r">விலை</th><th class="r">தொகை</th></tr></thead><tbody>' + items + '</tbody></table></div>' +
      '<div class="tot"><span>மொத்தம்</span><span class="r">' + inr(sub(c)) + '</span>' + (+c.discount ? '<span>தள்ளுபடி</span><span class="r">− ' + inr(c.discount) + '</span>' : '') +
      '<span class="big">இறுதி மதிப்பீடு</span><span class="r big">' + inr(total(c)) + '</span><span>பெற்றது</span><span class="r">' + inr(paid(c)) + '</span><span class="due">பாக்கி</span><span class="r due">' + inr(due(c)) + '</span></div></div>' +
      '<div class="sec"><h3>பெற்ற தொகை</h3>' + pays +
      '<form data-form="pay" data-id="' + c.id + '" class="grid"><div class="fld"><label>தொகை (₹)</label><input name="amt" type="number" min="1" inputmode="numeric" required></div>' +
      '<div class="fld"><label>தேதி</label><input name="date" type="date" value="' + today() + '" required></div>' +
      '<div class="fld span2"><label>குறிப்பு</label><input name="note" placeholder="முன்பணம் / UPI / பணம்"></div>' +
      '<button class="btn primary span2" type="submit">தொகை சேர்</button></form></div>' +
      '<div class="sec"><h3>WhatsApp செய்தி</h3><textarea class="prev" id="wamsg">' + esc(message(c)) + '</textarea>' +
      '<div class="row wrapx"><a class="btn primary" id="wasend" href="' + waLink(c, message(c)) + '" target="_blank" rel="noopener">WhatsApp-ல் அனுப்பு</a><button class="btn" type="button" data-act="copymsg">நகலெடு</button></div></div>';
    drawer(c.name, body,
      '<button class="btn" type="button" data-act="edit" data-id="' + c.id + '">திருத்து</button><button class="btn danger" type="button" data-act="delete" data-id="' + c.id + '">நீக்கு</button>');
  }

  /* ---------- form ---------- */
  function jobRow(j, i) {
    j = j || { type: JOBS[0], colour: '', hex: '#dddddd', area: '', rate: '' };
    return '<div class="item" data-job="' + i + '"><div class="grid">' +
      '<div class="fld"><label>வேலை வகை</label><select name="type">' + JOBS.map(function (t) { return '<option' + (t === j.type ? ' selected' : '') + '>' + t + '</option>'; }).join('') + (JOBS.indexOf(j.type) < 0 ? '<option selected>' + esc(j.type) + '</option>' : '') + '</select></div>' +
      '<div class="fld"><label>நிறம் (பெயர் / எண்)</label><div class="row"><input type="color" name="hex" value="' + esc(j.hex || '#dddddd') + '"><input name="colour" value="' + esc(j.colour) + '" placeholder="எ.கா. ஸ்கை ப்ளூ 2207"></div></div>' +
      '<div class="fld"><label>ச.அடி</label><input name="area" type="number" min="0" step="any" inputmode="decimal" value="' + esc(j.area) + '"></div>' +
      '<div class="fld"><label>ஒரு ச.அடி விலை (₹)</label><input name="rate" type="number" min="0" step="any" inputmode="decimal" value="' + esc(j.rate) + '"></div></div>' +
      '<div class="item-foot"><button class="btn small danger" type="button" data-act="rmjob">நீக்கு</button><b class="lt">' + inr(lineAmt(j)) + '</b></div></div>';
  }
  function form(c) {
    var n = !c; c = c || { name: '', phone: '', place: '', stage: 'enquiry', jobs: [null], discount: 0, visit: '', follow: addDays(2), notes: '', payments: [] };
    var body = '<form id="cform" data-id="' + (n ? '' : c.id) + '" style="display:flex;flex-direction:column;gap:16px">' +
      '<div class="sec"><div class="grid">' +
      '<div class="fld"><label>பெயர் *</label><input name="name" required value="' + esc(c.name) + '"></div>' +
      '<div class="fld"><label>தொலைபேசி *</label><input name="phone" type="tel" inputmode="tel" required pattern="[0-9+ ]{10,15}" value="' + esc(c.phone) + '"><span class="hint">10 இலக்க எண்</span></div>' +
      '<div class="fld"><label>இடம்</label><input name="place" value="' + esc(c.place) + '"></div>' +
      '<div class="fld"><label>நிலை</label><select name="stage">' + stageOpts(c.stage) + '</select></div>' +
      '<div class="fld"><label>தள ஆய்வு தேதி</label><input name="visit" type="date" value="' + esc(c.visit) + '"></div>' +
      '<div class="fld"><label>பின்தொடர்தல் தேதி</label><input name="follow" type="date" value="' + esc(c.follow) + '"></div>' +
      '<div class="fld span2"><label>குறிப்பு</label><textarea name="notes">' + esc(c.notes) + '</textarea></div></div></div>' +
      '<div class="sec"><h3>வேலைகள்</h3><div id="jobs" style="display:flex;flex-direction:column;gap:10px">' + c.jobs.map(jobRow).join('') + '</div>' +
      '<button class="btn" type="button" data-act="addjob">+ வேலை சேர்</button>' +
      '<div class="grid"><div class="fld"><label>தள்ளுபடி (₹)</label><input name="discount" type="number" min="0" inputmode="numeric" value="' + (+c.discount || '') + '"></div></div>' +
      '<div class="sum"><span>மொத்தம்</span><span class="r" id="fsub"></span><span class="big">இறுதி மதிப்பீடு</span><span class="r big" id="ftot"></span></div></div></form>';
    drawer(n ? 'புதிய வாடிக்கையாளர்' : 'திருத்து', body,
      '<button class="btn primary" type="submit" form="cform">சேமி</button><button class="btn" type="button" data-act="close">ரத்து</button>');
    recalc();
  }
  function readJobs() {
    return Array.prototype.map.call(document.querySelectorAll('#jobs .item'), function (el) {
      var g = function (n) { return el.querySelector('[name=' + n + ']').value; };
      return { type: g('type'), colour: g('colour').trim(), hex: g('hex'), area: +g('area') || 0, rate: +g('rate') || 0 };
    });
  }
  function recalc() {
    var jobs = readJobs(), s = 0;
    document.querySelectorAll('#jobs .item').forEach(function (el, i) { var a = lineAmt(jobs[i]); s += a; el.querySelector('.lt').textContent = inr(a); });
    var d = +(document.querySelector('#cform [name=discount]') || {}).value || 0;
    $('#fsub').textContent = inr(s); $('#ftot').textContent = inr(Math.max(0, s - d));
  }

  /* ---------- settings ---------- */
  function settingsView() {
    var body = '<form id="sform" style="display:flex;flex-direction:column;gap:16px"><div class="sec"><div class="grid">' +
      '<div class="fld span2"><label>நிறுவனப் பெயர்</label><input name="name" value="' + esc(settings.name) + '"></div>' +
      '<div class="fld span2"><label>முகவரி</label><textarea name="address">' + esc(settings.address) + '</textarea></div>' +
      '<div class="fld"><label>தொலைபேசி</label><input name="phone" value="' + esc(settings.phone) + '"></div>' +
      '<div class="fld"><label>தோற்றம்</label><select name="theme"><option value="auto"' + (settings.theme === 'auto' ? ' selected' : '') + '>தானியங்கி</option><option value="light"' + (settings.theme === 'light' ? ' selected' : '') + '>வெளிச்சம்</option><option value="dark"' + (settings.theme === 'dark' ? ' selected' : '') + '>இருண்டது</option></select></div></div></div>' +
      '<div class="sec"><h3>காப்புப் பிரதி</h3><p class="hint">தரவு இந்த உலாவியில் மட்டுமே சேமிக்கப்படுகிறது. உலாவித் தரவை அழித்தால் போய்விடும்; அடிக்கடி காப்புப் பிரதி எடுக்கவும்.</p>' +
      '<div class="row wrapx"><button class="btn" type="button" data-act="backup">காப்புப் பிரதி (JSON)</button><button class="btn" type="button" data-act="restore">மீட்டெடு</button><button class="btn" type="button" data-act="export">CSV சேமி</button></div></div>' +
      '<div class="sec"><h3>ஆபத்தான பகுதி</h3><button class="btn danger" type="button" data-act="wipe">அனைத்து தரவையும் நீக்கு</button></div></form>';
    drawer('அமைப்புகள்', body, '<button class="btn primary" type="submit" form="sform">சேமி</button><button class="btn" type="button" data-act="close">மூடு</button>');
  }

  /* ---------- helpers ---------- */
  function get(id) { return customers.filter(function (c) { return c.id === id; })[0]; }
  function toast(t) { var e = $('#toast'); e.textContent = t; e.hidden = false; clearTimeout(toast.t); toast.t = setTimeout(function () { e.hidden = true; }, 2600); }
  function applyTheme() { var r = document.documentElement; if (settings.theme === 'auto') r.removeAttribute('data-theme'); else r.setAttribute('data-theme', settings.theme); }
  function download(name, text, type) {
    var a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([text], { type: type })); a.download = name;
    document.body.appendChild(a); a.click(); a.remove(); setTimeout(function () { URL.revokeObjectURL(a.href); }, 1000);
  }
  function csv() {
    var q = function (v) { return '"' + String(v == null ? '' : v).replace(/"/g, '""') + '"'; };
    var rows = [['பெயர்', 'தொலைபேசி', 'இடம்', 'நிலை', 'வேலை', 'மதிப்பீடு', 'பெற்றது', 'பாக்கி', 'தள ஆய்வு', 'பின்தொடர்தல்', 'குறிப்பு']];
    customers.forEach(function (c) {
      rows.push([c.name, c.phone, c.place, stageName(c.stage), c.jobs.map(function (j) { return j.type + (j.colour ? ' (' + j.colour + ')' : '') + ' ' + j.area + 'x' + j.rate; }).join('; '), total(c), paid(c), due(c), c.visit, c.follow, c.notes]);
    });
    return '﻿' + rows.map(function (r) { return r.map(q).join(','); }).join('\r\n');
  }

  /* ---------- events ---------- */
  document.addEventListener('click', function (e) {
    var t = e.target;
    var act = t.closest('[data-act]');
    if (act) {
      var a = act.getAttribute('data-act'), id = act.getAttribute('data-id');
      if (a === 'add') form();
      else if (a === 'close') closeDrawer();
      else if (a === 'detail') { e.stopPropagation(); detail(id); }
      else if (a === 'edit') form(get(id));
      else if (a === 'settings') settingsView();
      else if (a === 'export') { download('skywall-customers.csv', csv(), 'text/csv;charset=utf-8'); toast('CSV சேமிக்கப்பட்டது'); }
      else if (a === 'backup') { download('skywall-backup-' + today() + '.json', JSON.stringify({ customers: customers, settings: settings }), 'application/json'); toast('காப்புப் பிரதி சேமிக்கப்பட்டது'); }
      else if (a === 'restore') $('#importFile').click();
      else if (a === 'clearex') { customers = customers.filter(function (c) { return !c.sample; }); save(); render(); }
      else if (a === 'wipe') { if (confirm('அனைத்து வாடிக்கையாளர் தரவும் நிரந்தரமாக நீக்கப்படும். உறுதியா?')) { customers = []; save(); closeDrawer(); render(); } }
      else if (a === 'delete') { if (confirm('இந்த வாடிக்கையாளரை நீக்கவா?')) { customers = customers.filter(function (c) { return c.id !== id; }); save(); closeDrawer(); render(); } }
      else if (a === 'delpay') { var c = get(id); c.payments = c.payments.filter(function (p) { return p.id !== act.getAttribute('data-pid'); }); save(); render(); detail(id); }
      else if (a === 'copymsg') { var v = $('#wamsg').value; (navigator.clipboard ? navigator.clipboard.writeText(v) : Promise.reject()).then(function () { toast('நகலெடுக்கப்பட்டது'); }, function () { $('#wamsg').select(); document.execCommand('copy'); toast('நகலெடுக்கப்பட்டது'); }); }
      else if (a === 'addjob') { $('#jobs').insertAdjacentHTML('beforeend', jobRow(null, 0)); recalc(); }
      else if (a === 'rmjob') { if (document.querySelectorAll('#jobs .item').length > 1) { act.closest('.item').remove(); recalc(); } }
      return;
    }
    var f = t.closest('[data-filter]');
    if (f) { filter = f.getAttribute('data-filter'); render(); return; }
    if (t.closest('select, a, button, input')) return;
    var o = t.closest('[data-open]');
    if (o) detail(o.getAttribute('data-open'));
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeDrawer();
    if (e.key === 'Enter' && e.target.matches('.card')) detail(e.target.getAttribute('data-open'));
  });
  document.addEventListener('change', function (e) {
    var t = e.target;
    if (t.matches('[data-stage-of]')) {
      var c = get(t.getAttribute('data-stage-of')); c.stage = t.value; save(); render();
      if ($('.drawer')) detail(c.id);
    } else if (t.id === 'importFile') {
      var file = t.files[0]; if (!file) return;
      var r = new FileReader();
      r.onload = function () {
        try {
          var d = JSON.parse(r.result);
          if (!d.customers || !Array.isArray(d.customers)) throw 0;
          if (!confirm('தற்போதைய தரவு மாற்றப்படும். தொடரவா?')) return;
          customers = d.customers; if (d.settings) { settings = Object.assign({}, DEF, d.settings); saveSettings(); }
          save(); closeDrawer(); render(); toast('மீட்டெடுக்கப்பட்டது');
        } catch (x) { toast('கோப்பு சரியில்லை'); }
      };
      r.readAsText(file); t.value = '';
    }
  });
  document.addEventListener('input', function (e) {
    if (e.target.id === 'search') renderList();
    else if (e.target.closest('#cform')) recalc();
    else if (e.target.id === 'wamsg') { var c = $('#wasend'); var cur = c.getAttribute('href').split('?')[0]; c.setAttribute('href', cur + '?text=' + encodeURIComponent(e.target.value)); }
  });
  $('#sort').addEventListener('change', renderList);
  document.addEventListener('submit', function (e) {
    e.preventDefault();
    var f = e.target, id = f.getAttribute('data-id');
    if (f.id === 'cform') {
      var v = function (n) { return f.querySelector('[name=' + n + ']').value.trim(); };
      var data = { name: v('name'), phone: v('phone'), place: v('place'), stage: v('stage'), visit: v('visit'), follow: v('follow'), notes: v('notes'), discount: +v('discount') || 0, jobs: readJobs() };
      if (id) { var c = get(id); Object.assign(c, data); save(); render(); detail(id); }
      else { var n = Object.assign({ id: uid(), created: Date.now(), payments: [] }, data); customers.push(n); save(); render(); detail(n.id); }
      toast('சேமிக்கப்பட்டது');
    } else if (f.id === 'sform') {
      ['name', 'address', 'phone', 'theme'].forEach(function (k) { settings[k] = f.querySelector('[name=' + k + ']').value.trim() || DEF[k]; });
      saveSettings(); closeDrawer(); render(); toast('சேமிக்கப்பட்டது');
    } else if (f.getAttribute('data-form') === 'pay') {
      var c2 = get(id), amt = +f.querySelector('[name=amt]').value;
      if (amt > 0) { c2.payments.push({ id: uid(), date: f.querySelector('[name=date]').value, amt: amt, note: f.querySelector('[name=note]').value.trim() }); save(); render(); detail(id); toast('தொகை சேர்க்கப்பட்டது'); }
    }
  });

  /* ---------- start ---------- */
  load(); render();
  if ('serviceWorker' in navigator && location.protocol !== 'file:') navigator.serviceWorker.register('sw.js').catch(function () {});
})();
