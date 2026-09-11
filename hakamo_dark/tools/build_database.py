#!/usr/bin/env python3
import csv, io, os, sqlite3, sys, urllib.request, datetime

URL = os.environ.get('HAKAMO_EGYPT_CSV_URL', 'https://raw.githubusercontent.com/karem505/egyptian-drug-database/main/data/egyptian-drugs.csv')
OUTS = ['database/hakamo_drugs.db', 'app/src/main/assets/hakamo_drugs.db']

os.makedirs('database', exist_ok=True)
os.makedirs('app/src/main/assets', exist_ok=True)
print('Downloading Egyptian medicine dataset...')
with urllib.request.urlopen(URL, timeout=90) as r:
    data = r.read()
print('Downloaded', len(data), 'bytes')

text = data.decode('utf-8-sig', errors='replace')
reader = csv.DictReader(io.StringIO(text))
rows = list(reader)
if not rows:
    raise SystemExit('Dataset is empty')

print('Rows:', len(rows))
print('Columns:', reader.fieldnames)

def pick(row, *names):
    for n in names:
        if n in row and row[n] not in (None, ''):
            return str(row[n]).strip()
    return ''

def norm_price(s):
    if not s: return ''
    return s.replace(',', '.').strip()

schema = '''CREATE TABLE IF NOT EXISTS drugs (
 id TEXT PRIMARY KEY,
 trade TEXT, trade_ar TEXT, generic TEXT, generic_ar TEXT,
 active TEXT, form TEXT, company TEXT, category TEXT,
 indications TEXT, source TEXT, updated_at TEXT,
 price_egp TEXT, price_updated_at TEXT, image_url TEXT
)'''

def build(path):
    if os.path.exists(path): os.remove(path)
    con = sqlite3.connect(path)
    con.execute(schema)
    con.execute('CREATE INDEX IF NOT EXISTS idx_trade ON drugs(trade)')
    con.execute('CREATE INDEX IF NOT EXISTS idx_generic ON drugs(generic)')
    con.execute('CREATE INDEX IF NOT EXISTS idx_active ON drugs(active)')
    con.execute('CREATE INDEX IF NOT EXISTS idx_company ON drugs(company)')
    con.execute('CREATE INDEX IF NOT EXISTS idx_category ON drugs(category)')
    now = datetime.date.today().isoformat()
    sql = '''INSERT OR REPLACE INTO drugs
      (id,trade,trade_ar,generic,generic_ar,active,form,company,category,indications,source,updated_at,price_egp,price_updated_at,image_url)
      VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)'''
    inserted = 0
    seen = set()
    for i, row in enumerate(rows, 1):
        trade = pick(row, 'commercial_name_en','trade_name_en','trade','brand_name')
        trade_ar = pick(row, 'commercial_name_ar','trade_name_ar','trade_ar')
        generic = pick(row, 'scientific_name','generic_name','generic')
        active = pick(row, 'active_ingredient','active_ingredients','substance_name') or generic
        company = pick(row, 'manufacturer','company','manufacturer_name')
        form = pick(row, 'dosage_form','form','dosage')
        category = pick(row, 'drug_class','category','therapeutic_class','pharmacological_class') or 'غير مصنف'
        indications = pick(row, 'indications','indications_and_usage','uses','purpose')
        price = norm_price(pick(row, 'price_egp','price','price_egp_current','current_price'))
        updated = pick(row, 'updated_at','last_updated','date') or now
        image_url = pick(row, 'image_url','image','image_link','product_image','photo_url')
        if not (trade or generic or active):
            continue
        base = '|'.join([trade, trade_ar, generic, active, company, form])
        idv = 'eg-' + __import__('hashlib').sha1(base.encode('utf-8')).hexdigest()
        if idv in seen: continue
        seen.add(idv)
        con.execute(sql, (idv, trade, trade_ar, generic, '', active, form, company, category,
                           indications, 'Egyptian Drug Database (CC0 dataset)', updated, price, updated, image_url))
        inserted += 1
    con.commit()
    con.close()
    print(path, 'records:', inserted)

for p in OUTS:
    build(p)
print('Done.')
