package me.maciekmm.FrameStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.v1_6_R1.NBTBase;
import net.minecraft.server.v1_6_R1.NBTTagCompound;
import net.minecraft.server.v1_6_R1.NBTTagList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_6_R1.inventory.CraftItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
/*
 * @author Comphenix||aadnk
 * source https://gist.github.com/aadnk/4102407
 */

public class Serializer {

    public static String toBase64(Inventory inventory) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        NBTTagList itemList = new NBTTagList();

        // Save every element in the list
        for (int i = 0; i < inventory.getSize(); i++) {
            NBTTagCompound outputObject = new NBTTagCompound();
            CraftItemStack craft = getCraftVersion(inventory.getItem(i));

            // Convert the item stack to a NBT compound
            if (craft != null) {
                CraftItemStack.asNMSCopy(craft).save(outputObject);
            }
            itemList.add(outputObject);
        }

        // Now save the list
        NBTBase.a(itemList, dataOutput);

        // Serialize that array
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    public static Inventory fromBase64(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        NBTTagList itemList = (NBTTagList) NBTBase.a(new DataInputStream(inputStream));
        Inventory inventory = new CraftInventoryCustom(null, itemList.size());

        for (int i = 0; i < itemList.size(); i++) {
            NBTTagCompound inputObject = (NBTTagCompound) itemList.get(i);

            if (!inputObject.isEmpty()) {
                inventory.setItem(i, CraftItemStack.asCraftMirror(
                        net.minecraft.server.v1_6_R1.ItemStack.createStack(inputObject)));
            }
        }

        // Serialize that array
        return inventory;
    }

    public static Inventory getInventoryFromArray(ItemStack[] items) {
        CraftInventoryCustom custom = new CraftInventoryCustom(null, items.length);

        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                custom.setItem(i, items[i]);
            }
        }
        return custom;
    }

    /**
     * @author maciekmm
     * @param l - location
     * @return String
     */
    public static String serializeLoc(Location l) {
        return l.getX() + "," + l.getY() + "," + l.getZ() + "," + l.getYaw() + "," + l.getPitch() + "," + l.getWorld().getName();
    }

    public static Location unserializeLoc(String l) {
        if (l == null) {
            return null;
        }
        String[] array = l.split(",");
        if (array.length < 5) {
            return null;
        }
        return new Location(Bukkit.getWorld(array[5]), Double.valueOf(array[0]), Double.valueOf(array[1]), Double.valueOf(array[2]), Float.valueOf(array[3]),
                Float.valueOf(array[4]));
    }

    /*
     * @author maciekmm
     */
    public static String serializeEnch(Map<Enchantment, Integer> im) {
        if (im != null) {
            StringBuilder sb = new StringBuilder();
            for (Entry thisEntry : im.entrySet()) {
                sb.append(((Enchantment) thisEntry.getKey()).getId()).append("@").append(thisEntry.getValue()).append("!");
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    public static String serializeLore(List<String> ls) {
        if (ls == null || ls.isEmpty()) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : ls) {
                sb.append(s).append("^$");
            }
            return sb.toString();
        }
    }

    public static Map<Enchantment, Integer> toItemMeta(String s) {
        if (s != null && !s.equalsIgnoreCase("null") && !s.equals("")) {
            Map<Enchantment, Integer> em = new HashMap<>();
            String[] eslist = s.split("!");
            for (int i = 0; i <= eslist.length - 1; i++) {
                String[] ese = eslist[i].split("@");
                em.put(Enchantment.getById(Integer.parseInt(ese[0])), Integer.parseInt(ese[1]));
            }
            return em;
        } else {
            return null;
        }
    }

    public static void addEnchantments(ItemStack is, Map<Enchantment, Integer> em) {
        ItemMeta im = is.getItemMeta();
        for (Entry thisEntry : em.entrySet()) {
            im.addEnchant((Enchantment) thisEntry.getKey(), (Integer) thisEntry.getValue(), true);
        }
        is.setItemMeta(im);
    }
    /*
     * @author Comphenix
     */

    private static CraftItemStack getCraftVersion(ItemStack stack) {
        if (stack instanceof CraftItemStack) {
            return (CraftItemStack) stack;
        } else if (stack != null) {
            return CraftItemStack.asCraftCopy(stack);
        } else {
            return null;
        }
    }
    /**
     * The Standard Item Syntax. This class is built to be able to survive on
     * its own, without CraftBook.
     *
     * @author Me4502
     *
     */
    private static final Pattern ASTERISK_PATTERN = Pattern.compile("*", Pattern.LITERAL);
    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(";", Pattern.LITERAL);
    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
    private static final Pattern PIPE_PATTERN = Pattern.compile("|", Pattern.LITERAL);
    private static final Pattern FSLASH_PATTERN = Pattern.compile("/", Pattern.LITERAL);

    /**
     * The opposite of {@link getItem()}. Returns the String made by an
     * {@link ItemStack}. This can be used in getItem() to return the same
     * {@link ItemStack}.
     *
     * @author me4502
     *
     * @param item The {@link ItemStack} to convert into a {@link String}.
     * @return The {@link String} that represents the {@link ItemStack}.
     */
    public static String getStringFromItem(ItemStack item) {

        StringBuilder builder = new StringBuilder();
        builder.append(item.getType().name());
        if (item.getDurability() > 0) {
            builder.append(":").append(item.getDurability());
        }

        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasEnchants()) {
                for (Entry<Enchantment, Integer> enchants : item.getItemMeta().getEnchants().entrySet()) {
                    builder.append(";").append(enchants.getKey().getName()).append(":").append(enchants.getValue());
                }
            }
            if (item.getItemMeta().hasDisplayName()) {
                builder.append("|").append(item.getItemMeta().getDisplayName());
            }
            if (item.getItemMeta().hasLore()) {
                List<String> list = item.getItemMeta().getLore();
                for (String s : list) {
                    builder.append("|").append(s);
                }
            }
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof SkullMeta) {
                if (((SkullMeta) meta).hasOwner()) {
                    builder.append("/player:").append(((SkullMeta) meta).getOwner());
                }
            } else if (meta instanceof BookMeta) {
                if (((BookMeta) meta).hasTitle()) {
                    builder.append("/title:").append(((BookMeta) meta).getTitle());
                }
                if (((BookMeta) meta).hasAuthor()) {
                    builder.append("/author:").append(((BookMeta) meta).getAuthor());
                }
                if (((BookMeta) meta).hasPages()) {
                    for (String page : ((BookMeta) meta).getPages()) {
                        builder.append("/page:").append(page);
                    }
                }
            } else if (meta instanceof LeatherArmorMeta) {
                if (!((LeatherArmorMeta) meta).getColor().equals(Bukkit.getItemFactory().getDefaultLeatherColor())) {
                    builder.append("/color:").append(((LeatherArmorMeta) meta).getColor().getRed()).append(",").append(((LeatherArmorMeta) meta).getColor().getGreen()).append(",").append(((LeatherArmorMeta) meta).getColor().getBlue());
                }
            } else if (meta instanceof PotionMeta) {
                if (!((PotionMeta) meta).hasCustomEffects()) {
                    for (PotionEffect eff : ((PotionMeta) meta).getCustomEffects()) {
                        builder.append("/potion:").append(eff.getType().getName()).append(";").append(eff.getDuration()).append(";").append(eff.getAmplifier());
                    }
                }
            } else if (meta instanceof EnchantmentStorageMeta) {
                if (!((EnchantmentStorageMeta) meta).hasStoredEnchants()) {
                    for (Entry<Enchantment, Integer> eff : ((EnchantmentStorageMeta) meta).getStoredEnchants().entrySet()) {
                        builder.append("/enchant:").append(eff.getKey().getName()).append(";").append(eff.getValue());
                    }
                }
            }
        }

        return builder.toString();
    }

    /**
     * Parse an item from a line of text.
     *
     * @author me4502
     *
     * @param line The line to parse it from.
     * @return The item to create.
     */
    public static ItemStack getItem(String line) {

        if (line == null || line.isEmpty()) {
            return null;
        }

        int id = 0;
        int data = 0;
        int amount = 1;

        String[] advMetadataSplit = FSLASH_PATTERN.split(line);
        String[] nameLoreSplit = PIPE_PATTERN.split(advMetadataSplit[0]);
        String[] enchantSplit = SEMICOLON_PATTERN.split(nameLoreSplit[0]);
        String[] amountSplit = ASTERISK_PATTERN.split(enchantSplit[0], 2);
        String[] dataSplit = COLON_PATTERN.split(amountSplit[0], 2);
        try {
            id = Integer.parseInt(dataSplit[0]);
        } catch (NumberFormatException e) {
            try {
                id = Material.matchMaterial(dataSplit[0]).getId();
                if (id < 1) {
                    id = 1;
                }
            } catch (Exception ee) {
                try {
                    try {
                        Object itemType = Class.forName("com.sk89q.worldedit.blocks.ItemType").getMethod("lookup", String.class).invoke(null, dataSplit[0]);
                        id = (Integer) itemType.getClass().getMethod("getID").invoke(itemType);
                        if (id < 1) {
                            id = 1;
                        }
                    } catch (Exception eee) {
                        Object blockType = Class.forName("com.sk89q.worldedit.blocks.BlockType").getMethod("lookup", String.class).invoke(null, dataSplit[0]);
                        id = (Integer) blockType.getClass().getMethod("getID").invoke(blockType);
                        if (id < 1) {
                            id = 1;
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        try {
            if (dataSplit.length > 1) {
                data = Integer.parseInt(dataSplit[1]);
            }
        } catch (Exception ignored) {
        }
        try {
            if (amountSplit.length > 1) {
                amount = Integer.parseInt(amountSplit[1]);
            }
        } catch (Exception ignored) {
        }

        id = Math.max(1, id);

        ItemStack rVal = new ItemStack(id, amount, (short) data);
        rVal.setData(new MaterialData(id, (byte) data));

        if (nameLoreSplit.length > 1 && id > 0) {

            ItemMeta meta = rVal.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameLoreSplit[1]));
            if (nameLoreSplit.length > 2) {

                List<String> lore = new ArrayList<String>();
                for (int i = 2; i < nameLoreSplit.length; i++) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', nameLoreSplit[i]));
                }

                meta.setLore(lore);
            }

            rVal.setItemMeta(meta);
        }
        if (enchantSplit.length > 1) {

            for (int i = 1; i < enchantSplit.length; i++) {

                try {
                    String[] sp = COLON_PATTERN.split(enchantSplit[i]);
                    Enchantment ench = Enchantment.getByName(sp[0]);
                    if (ench == null) {
                        ench = Enchantment.getById(Integer.parseInt(sp[0]));
                    }
                    rVal.addUnsafeEnchantment(ench, Integer.parseInt(sp[1]));
                } catch (NumberFormatException ignored) {
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
        if (advMetadataSplit.length > 1) {

            ItemMeta meta = rVal.getItemMeta();
            for (int i = 1; i < advMetadataSplit.length; i++) {
                String section = advMetadataSplit[i];
                String[] bits = COLON_PATTERN.split(section, 2);
                if (bits.length < 2) {
                    continue; //Invalid Bit.
                } else if (bits[0].equalsIgnoreCase("player") && meta instanceof SkullMeta) {
                    ((SkullMeta) meta).setOwner(bits[1]);
                } else if (bits[0].equalsIgnoreCase("author") && meta instanceof BookMeta) {
                    ((BookMeta) meta).setAuthor(bits[1]);
                } else if (bits[0].equalsIgnoreCase("title") && meta instanceof BookMeta) {
                    ((BookMeta) meta).setTitle(bits[1]);
                } else if (bits[0].equalsIgnoreCase("page") && meta instanceof BookMeta) {
                    ((BookMeta) meta).addPage(bits[1]);
                } else if (bits[0].equalsIgnoreCase("color") && meta instanceof LeatherArmorMeta) {

                    String[] cols = COMMA_PATTERN.split(bits[1]);
                    ((LeatherArmorMeta) meta).setColor(org.bukkit.Color.fromRGB(Integer.parseInt(cols[0]), Integer.parseInt(cols[1]), Integer.parseInt(cols[2])));
                } else if (bits[0].equalsIgnoreCase("potion") && meta instanceof PotionMeta) {

                    String[] effects = SEMICOLON_PATTERN.split(bits[1]);
                    try {
                        PotionEffect effect = new PotionEffect(PotionEffectType.getByName(effects[0]), Integer.parseInt(effects[1]), Integer.parseInt(effects[2]));
                        ((PotionMeta) meta).addCustomEffect(effect, true);
                    } catch (Exception ignored) {
                    }
                } else if (bits[0].equalsIgnoreCase("enchant") && meta instanceof EnchantmentStorageMeta) {
                    try {
                        String[] sp = SEMICOLON_PATTERN.split(bits[1]);
                        Enchantment ench = Enchantment.getByName(sp[0]);
                        if (ench == null) {
                            ench = Enchantment.getById(Integer.parseInt(sp[0]));
                        }
                        ((EnchantmentStorageMeta) meta).addStoredEnchant(ench, Integer.parseInt(sp[1]), true);
                    } catch (Exception ignored) {
                    }
                }
            }
            rVal.setItemMeta(meta);
        }

        return rVal;
    }
}