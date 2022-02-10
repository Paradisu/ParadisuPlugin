import yaml

with open('warps.yml', encoding="utf8") as f:
    data = yaml.load(f, Loader=yaml.SafeLoader)

#print(data['1']);


statements = []
template = 'INSERT IGNORE INTO Warps (X_pos, Y_pos, Z_pos, Pitch, Yaw, World, Permission, DisplayName, WarpName) VALUES ({}, {}, {}, {}, {}, "{}", "{}", "{}", "{}");'
for key in data:
    #check if data[key]['display'] is null
    
    if 'display' in data[key]:
        holder = data[key]['display'];
    else:
        holder = 'NULL';

    statement = template.format(data[key]['X'], data[key]['Y'], data[key]['Z'], data[key]['Yaw'], data[key]['Pitch'], data[key]['World'], data[key]['Permission'], holder, key)
    print(statement)
    statements.append(statement)
    
        
        
        
#statements = [template.format(nv, nv[0], nv[1], nv[2], nv[3], nv[4], nv[5], nv[6], nv[7], nv[8], nv[9]) for nv in data]


with open('result.sql', 'w', encoding="utf8") as f:
    for statment in statements:
        f.write(statment + '\n')

