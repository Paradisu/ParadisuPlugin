import yaml

with open('propmodels.yml') as f:
    data = yaml.load(f, Loader=yaml.SafeLoader)

template = 'INSERT IGNORE INTO PropModels (id, displayname, enchantslot1, enchantslot2, enchantslot3, ehcnatslot1level, enchantslot2level, enchantslot3level, unbreakable, hideunbreakable, hideenchants) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);'
statements = [template.format(nv, nv['displayname'], nv['enchantslot1'], nv['enchantslot2'], nv['enchantslot3'], nv['enchantslot1level'], nv['enchantslot2level'], nv['enchantslot3level'], nv['unbreakable'], nv['hideunbreakable'], nv['hideenchants']) for nv in data]

with open('result.sql', 'w') as f:
    for statment in statements:
        f.write(statment + '\n')

