import yaml

with open('propmodels.yml', encoding="utf8") as f:
    data = yaml.load(f, Loader=yaml.SafeLoader)

#print(data['1']);


statements = []
template = 'INSERT IGNORE INTO PropModels (id, displayname, enchantslot1, enchantslot2, enchantslot3, enchantslot1level, enchantslot2level, enchantslot3level, unbreakable, hideunbreakable, hideenchants) VALUES ({}, "{}", "{}", "{}", "{}", {}, {}, {}, {}, {}, {});'
for key in data:
    statement = template.format(key, data[key]['displayname'], data[key]['enchantslot1'], data[key]['enchantslot2'], data[key]['enchantslot3'], data[key]['enchantslot1level'], data[key]['enchantslot2level'], data[key]['enchantslot3level'], data[key]['unbreakable'], data[key]['hideunbreakable'], data[key]['hideenchants'])
    print(statement)
    statements.append(statement)
        
        
        
#statements = [template.format(nv, nv[0], nv[1], nv[2], nv[3], nv[4], nv[5], nv[6], nv[7], nv[8], nv[9]) for nv in data]


with open('result.sql', 'w', encoding="utf8") as f:
    for statment in statements:
        f.write(statment + '\n')

