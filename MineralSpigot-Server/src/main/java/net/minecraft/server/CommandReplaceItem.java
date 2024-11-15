package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;

public class CommandReplaceItem extends CommandAbstract {

    private static final Object2IntOpenHashMap<String> a = new Object2IntOpenHashMap<>();

    public CommandReplaceItem() {
    }

    public String getCommand() {
        return "replaceitem";
    }

    public int a() {
        return 2;
    }

    public String getUsage(ICommandListener icommandlistener) {
        return "commands.replaceitem.usage";
    }

    public void execute(ICommandListener icommandlistener, String[] astring) throws CommandException {
        if (astring.length < 1) {
            throw new ExceptionUsage("commands.replaceitem.usage", new Object[0]);
        } else {
            boolean flag;

            if (astring[0].equals("entity")) {
                flag = false;
            } else {
                if (!astring[0].equals("block")) {
                    throw new ExceptionUsage("commands.replaceitem.usage", new Object[0]);
                }

                flag = true;
            }

            byte b0;

            if (flag) {
                if (astring.length < 6) {
                    throw new ExceptionUsage("commands.replaceitem.block.usage", new Object[0]);
                }

                b0 = 4;
            } else {
                if (astring.length < 4) {
                    throw new ExceptionUsage("commands.replaceitem.entity.usage", new Object[0]);
                }

                b0 = 2;
            }

            int i = b0 + 1;
            int j = this.e(astring[b0]);

            Item item;

            try {
                item = f(icommandlistener, astring[i]);
            } catch (ExceptionInvalidNumber exceptioninvalidnumber) {
                if (Block.getByName(astring[i]) != Blocks.AIR) {
                    throw exceptioninvalidnumber;
                }

                item = null;
            }

            ++i;
            int k = astring.length > i ? a(astring[i++], 1, 64) : 1;
            int l = astring.length > i ? a(astring[i++]) : 0;
            ItemStack itemstack = new ItemStack(item, k, l);

            if (astring.length > i) {
                String s = a(icommandlistener, astring, i).c();

                try {
                    itemstack.setTag(MojangsonParser.parse(s));
                } catch (MojangsonParseException mojangsonparseexception) {
                    throw new CommandException("commands.replaceitem.tagError",
                            new Object[] { mojangsonparseexception.getMessage() });
                }
            }

            if (itemstack.getItem() == null) {
                itemstack = null;
            }

            if (flag) {
                icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.AFFECTED_ITEMS, 0);
                BlockPosition blockposition = a(icommandlistener, astring, 1, false);
                World world = icommandlistener.getWorld();
                TileEntity tileentity = world.getTileEntity(blockposition);

                if (tileentity == null || !(tileentity instanceof IInventory)) {
                    throw new CommandException("commands.replaceitem.noContainer",
                            new Object[] { Integer.valueOf(blockposition.getX()), Integer.valueOf(blockposition.getY()),
                                    Integer.valueOf(blockposition.getZ()) });
                }

                IInventory iinventory = (IInventory) tileentity;

                if (j >= 0 && j < iinventory.getSize()) {
                    iinventory.setItem(j, itemstack);
                }
            } else {
                Entity entity = b(icommandlistener, astring[1]);

                icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.AFFECTED_ITEMS, 0);
                if (entity instanceof EntityHuman) {
                    ((EntityHuman) entity).defaultContainer.b();
                }

                if (!entity.d(j, itemstack)) {
                    throw new CommandException("commands.replaceitem.failed", new Object[] { Integer.valueOf(j),
                            Integer.valueOf(k), itemstack == null ? "Air" : itemstack.C() });
                }

                if (entity instanceof EntityHuman) {
                    ((EntityHuman) entity).defaultContainer.b();
                }
            }

            icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.AFFECTED_ITEMS, k);
            a(icommandlistener, this, "commands.replaceitem.success",
                    new Object[] { Integer.valueOf(j), Integer.valueOf(k), itemstack == null ? "Air" : itemstack.C() });
        }
    }

    private int e(String s) throws CommandException {
        if (!CommandReplaceItem.a.containsKey(s)) {
            throw new CommandException("commands.generic.parameter.invalid", new Object[] { s });
        } else {
            return CommandReplaceItem.a.getInt(s);
        }
    }

    public List<String> tabComplete(ICommandListener icommandlistener, String[] astring, BlockPosition blockposition) {
        return astring.length == 1 ? a(astring, new String[] { "entity", "block" })
                : (astring.length == 2 && astring[0].equals("entity") ? a(astring, this.d())
                        : (astring.length >= 2 && astring.length <= 4 && astring[0].equals("block")
                                ? a(astring, 1, blockposition)
                                : ((astring.length != 3 || !astring[0].equals("entity"))
                                        && (astring.length != 5 || !astring[0].equals("block"))
                                                ? ((astring.length != 4 || !astring[0].equals("entity"))
                                                        && (astring.length != 6 || !astring[0].equals("block")) ? null
                                                                : a(astring, Item.REGISTRY.keySet()))
                                                : a(astring, CommandReplaceItem.a.keySet()))));
    }

    protected String[] d() {
        return MinecraftServer.getServer().getPlayers();
    }

    public boolean isListStart(String[] astring, int i) {
        return astring.length > 0 && astring[0].equals("entity") && i == 1;
    }

    static {
        int i;

        for (i = 0; i < 54; ++i) {
            CommandReplaceItem.a.put("slot.container." + i, i);
        }

        for (i = 0; i < 9; ++i) {
            CommandReplaceItem.a.put("slot.hotbar." + i, i);
        }

        for (i = 0; i < 27; ++i) {
            CommandReplaceItem.a.put("slot.inventory." + i, 9 + i);
        }

        for (i = 0; i < 27; ++i) {
            CommandReplaceItem.a.put("slot.enderchest." + i, 200 + i);
        }

        for (i = 0; i < 8; ++i) {
            CommandReplaceItem.a.put("slot.villager." + i, 300 + i);
        }

        for (i = 0; i < 15; ++i) {
            CommandReplaceItem.a.put("slot.horse." + i, 500 + i);
        }

        CommandReplaceItem.a.put("slot.weapon", 99);
        CommandReplaceItem.a.put("slot.armor.head", 103);
        CommandReplaceItem.a.put("slot.armor.chest", 102);
        CommandReplaceItem.a.put("slot.armor.legs", 101);
        CommandReplaceItem.a.put("slot.armor.feet", 100);
        CommandReplaceItem.a.put("slot.horse.saddle", 400);
        CommandReplaceItem.a.put("slot.horse.armor", 401);
        CommandReplaceItem.a.put("slot.horse.chest", 499);
    }
}
