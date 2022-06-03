package net.paradisu.ParadisuPlugin.util.protocollib;

// import com.comphenix.protocol.PacketType;
// import com.comphenix.protocol.ProtocolManager;
// import com.comphenix.protocol.events.PacketContainer;
// import com.comphenix.protocol.wrappers.EnumWrappers;
// import com.comphenix.protocol.wrappers.Pair;
// import com.comphenix.protocol.wrappers.WrappedDataWatcher;
// import com.comphenix.protocol.wrappers.WrappedWatchableObject;

// import java.lang.reflect.Method;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.UUID;

// /*import net.minecraft.core.IRegistry;
// import net.minecraft.world.entity.EntityTypes;
// import net.minecraft.world.entity.EnumItemSlot; */
// import org.bukkit.Bukkit;
// import org.bukkit.Location;
// import org.bukkit.entity.EntityType;
// import org.bukkit.inventory.ItemStack;

public class ProtoLibEntityHandling {



//         private static final Map<EntityType, Integer> objects = new HashMap<EntityType, Integer>() {    };

//         private static int lastId = 272183999;

//         private static ProtocolManager manager;

//         private static Method getNMSCopy;

//         public static void init(ProtocolManager manager) {
//             ProtoLib_Entity_Handling.manager = manager;
//             String version = Bukkit.getServer().getClass().getPackage().getName();
//             version = version.substring(version.lastIndexOf('.') + 1);
//             try {
//                 getNMSCopy = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack").getDeclaredMethod("asNMSCopy", new Class[] { ItemStack.class });
//             } catch (NoSuchMethodException|ClassNotFoundException e) {
//                 getNMSCopy = null;
//             }
//         }

//         public static List<PacketContainer> getSpawnPacket(FakeEntity entity) {
//             return getSpawnPacket(entity, entity.location, new WrappedDataWatcher());
//         }

//         public static List<PacketContainer> getSpawnPacket(FakeEntity entity, Location l, WrappedDataWatcher metadata) {
//             return getSpawnPacket(entity, l.getX(), l.getY(), l.getZ(), l.getPitch(), l.getYaw(), metadata);
//         }

//         public static List<PacketContainer> getSpawnPacket(FakeEntity entity, double x, double y, double z) {
//             return getSpawnPacket(entity, x, y, z, 0.0D, 0.0D, new WrappedDataWatcher());
//         }

//         public static List<PacketContainer> getSpawnPacket(FakeEntity entity, double x, double y, double z, WrappedDataWatcher metadata) {
//             return getSpawnPacket(entity, x, y, z, 0.0D, 0.0D, metadata);
//         }

//         public static List<PacketContainer> getSpawnPacket(FakeEntity entity, double x, double y, double z, double pitch, double yaw) {
//             return getSpawnPacket(entity, x, y, z, pitch, yaw, new WrappedDataWatcher());
//         }

//         public static PacketContainer moveEntityTo(FakeEntity entity, Location newLoc) {
//             PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
//             packet.getIntegers().write(0, entity.entityId);
//             entity.location = newLoc;
//             packet.getDoubles().write(0, newLoc.getX());
//             packet.getDoubles().write(1, newLoc.getY());
//             packet.getDoubles().write(2, newLoc.getZ());
//             packet.getBytes().write(0, (byte) (int) (newLoc.getYaw() * 256.0D / 360.0D));
//             packet.getBytes().write(1, (byte) (int) (newLoc.getPitch() * 256.0D / 360.0D));
//             packet.getBooleans().write(0, Boolean.FALSE);
//             return packet;
//         }

//         public static List<PacketContainer> getSpawnPacket(FakeEntity entity, double x, double y, double z, double pitch, double yaw, WrappedDataWatcher metadata) {
//             PacketContainer packet = new PacketContainer(entity.object ? PacketType.Play.Server.SPAWN_ENTITY : PacketType.Play.Server.SPAWN_ENTITY_LIVING);
//             packet.getIntegers().write(0, entity.entityId);
//             packet.getModifier().writeDefaults();
//             packet.getUUIDs().write(0, entity.uniqueId);
//             packet.getDoubles().write(0, x);
//             packet.getDoubles().write(1, y);
//             packet.getDoubles().write(2, z);
//             if (entity.object) {
//                 packet.getIntegers().write(6, entity.getTypeId());
//                 packet.getIntegers().write(4, (int) (pitch * 256.0D / 360.0D));
//                 packet.getIntegers().write(5, (int) (yaw * 256.0D / 360.0D));
//                 packet.getIntegers().write(7, entity.objectData);
//             } else {
//                 packet.getIntegers().write(1, entity.getTypeId());
//                 packet.getBytes().write(0, (byte) (int) (yaw * 256.0D / 360.0D));
//                 packet.getBytes().write(1, (byte) (int) (pitch * 256.0D / 360.0D));
//                 packet.getBytes().write(2, (byte) (int) (pitch * 256.0D / 360.0D));
//                 packet.getIntegers().write(2, 0);
//                 packet.getIntegers().write(3, 0);
//                 packet.getIntegers().write(4, 0);
//             }
//             List<PacketContainer> spawnPackets = new ArrayList<>();
//             spawnPackets.add(packet);
//             spawnPackets.add(getMetadataPacket(entity, metadata));

//             ArrayList equipment = new ArrayList();
//             for (EnumWrappers.ItemSlot slot : entity.equipment.keySet()) {
//                 ItemStack value = (ItemStack)entity.equipment.get(slot);
//                 equipment.add(new Pair<>(slot, value));
//             }

//             PacketContainer equipContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
//             equipContainer.getIntegers().write(0, entity.entityId);
//             equipContainer.getSlotStackPairLists().write(0, equipment);

//             if (!equipment.isEmpty()) {
//                 spawnPackets.add(equipContainer);
//             }

//             return spawnPackets;
//         }

//         public static PacketContainer getMetadataPacket(FakeEntity entity, WrappedDataWatcher metadata) {
//             return getMetadataPacket(entity, metadata.getWatchableObjects());
//         }

//         public static PacketContainer getMetadataPacket(FakeEntity entity, List<WrappedWatchableObject> metadata) {
//             PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//             packet.getModifier().writeDefaults();
//             packet.getModifier().write(0, Integer.valueOf(entity.entityId));
//             packet.getWatchableCollectionModifier().write(0, metadata);
//             return packet;
//         }

//         public static PacketContainer getPassengerPacket(FakeEntity entity, int[] passengers) {
//             PacketContainer packet = manager.createPacket(PacketType.Play.Server.MOUNT);
//             packet.getIntegers().write(0, entity.entityId);
//             packet.getIntegerArrays().write(0, passengers);
//             return packet;
//         }

//         public static PacketContainer getDespawnPacket(FakeEntity... entities) {
//             List ids = new ArrayList();
//             for (int i = 0; i < entities.length; i++) {
//                 ids.add(entities[i].entityId);
//             }
//             PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
//             packet.getIntLists().write(0, ids);
//             return packet;
//         }

//         public static PacketContainer getAttachPacket(FakeEntity entity, FakeEntity holding) {
//             PacketContainer packet = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
//             packet.getIntegers().write(0, entity.getEntityId());
//             packet.getIntegers().write(1, holding != null ? holding.getEntityId() : -1);
//             return packet;
//         }

//         public static WrappedDataWatcher createWatcher(Map<Integer, Object> data) {
//             WrappedDataWatcher watcher = new WrappedDataWatcher();
//             for (Map.Entry<Integer, Object> e : data.entrySet()) {
//                 if (e.getValue().getClass() == ItemStack.class) {
//                     if (getNMSCopy == null)
//                         throw new IllegalArgumentException("ItemStack serializer could not be found!");
//                     try {
//                         watcher.setObject(((Integer)e.getKey()).intValue(), WrappedDataWatcher.Registry.getItemStackSerializer(false), getNMSCopy.invoke(null, new Object[] { e.getValue() }));
//                     } catch (IllegalAccessException|java.lang.reflect.InvocationTargetException e1) {
//                         e1.printStackTrace();
//                     }
//                     continue;
//                 }
//                 watcher.setObject(((Integer)e.getKey()).intValue(), WrappedDataWatcher.Registry.get(e.getValue().getClass()), e.getValue());
//             }
//             return watcher;
//         }

//         public static void setDataByte(WrappedDataWatcher wdw, int index, int value) {
//             wdw.setObject(index, WrappedDataWatcher.Registry.get(Byte.class), Byte.valueOf((byte)value));
//         }

//         public static class FakeEntity {
//             private int entityId;
//             private UUID uniqueId;
//             private EntityType type;
//             private int typeId;
//             private boolean object;
//             private int objectData;
//             private Location location;
//             private Map<EnumWrappers.ItemSlot, ItemStack> equipment = new HashMap<>();

//             public static FakeEntity createNew(Location location, EntityType type, int objectData) {
//                 FakeEntity out = new FakeEntity();
//                 ProtoLib_Entity_Handling.lastId++;
//                 out.entityId = ProtoLib_Entity_Handling.lastId;
//                 out.uniqueId = UUID.randomUUID();
//                 out.type = type;
//                 out.typeId = ProtoLib_Entity_Handling.Y.getId(ProtoLib_Entity_Handling.a(type.getName()).get());
//                 out.object = ProtoLib_Entity_Handling.objects.containsKey(type);
//                 out.objectData = objectData;
//                 out.location = location;

//                 return out;
//             }

//             public static FakeEntity createNew(Location location, EntityType type) {
//                 return createNew(location, type, 0);
//             }

//             public Location getLocation() {
//                 return this.location;
//             }

//             public UUID getUniqueId() {
//                 return uniqueId;
//             }

//             public int getEntityId() {
//                 return entityId;
//             }

//             public EntityType getType() {
//                 return type;
//             }

//             public int getTypeId() {
//                 return typeId;
//             }

//             public void setEquipment(EnumWrappers.ItemSlot slot, ItemStack stack) {
//                 this.equipment.put(slot, stack);
//             }

//         }
}

