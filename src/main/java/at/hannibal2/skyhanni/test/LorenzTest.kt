package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.utils.*
import at.hannibal2.skyhanni.utils.GuiRender.renderString
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.getSBItemID
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class LorenzTest {

    var packetLog = LorenzLogger("debug/packets")

    companion object {
        var enabled = false
        var text = ""

        val debugLogger = LorenzLogger("debug/test")

        fun printLore() {
            try {
                val itemStack = Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem()!!
                print("===")
                print("ITEM LORE")
                print("display name: '" + itemStack.displayName.toString() + "'")
                val itemID = itemStack.getSBItemID()
                print("itemID: '$itemID'")
//            val rarity: ItemRarityOld = ItemUtils.getRarity(itemStack)
//            print("rarity: '$rarity'")
                print("")
                for (line in itemStack.getLore()) {
                    print("'$line'")
                    println(line)
                }
                print("")
                print("getTagCompound")
                if (itemStack.hasTagCompound()) {
                    val tagCompound = itemStack.tagCompound
                    for (s in tagCompound.keySet) {
                        print("  '$s'")
                    }
                    if (tagCompound.hasKey("ExtraAttributes")) {
                        print("")
                        print("ExtraAttributes")
                        val extraAttributes = tagCompound.getCompoundTag("ExtraAttributes")
//                    for (s in extraAttributes.keySet) {
//                        print("  '$s'")
//                    }
//                    if (extraAttributes.hasKey("enchantments")) {
//                        print("")
//                        print("enchantments")
//                        val enchantments = extraAttributes.getCompoundTag("enchantments")
//                        for (s in enchantments.keySet) {
//                            val level = enchantments.getInteger(s)
//                            print("  '$s' = $level")
//                        }
//                    }
//                    if (extraAttributes.hasKey("modifier")) {
//                        print("")
//                        print("modifier")
//                        val enchantments = extraAttributes.getCompoundTag("modifier")
//                        for (s in enchantments.keySet) {
//                            print("  '$s'")
//                        }
//                    }

                        runn(extraAttributes, "  .  ")
                    }
                }
                print("")
                print("===")
                LorenzUtils.debug("item info printed!")
            } catch (_: Throwable) {
                LorenzUtils.error("Hold an item in the hand to see its item infos!")
            }
        }

        fun runn(compound: NBTTagCompound, text: String) {
            print("$text'$compound'")
            for (s in compound.keySet) {
                val element = compound.getCompoundTag(s)
                runn(element, "$text  ")
            }
        }

        private fun print(text: String) {
            LorenzDebug.log(text)
        }

        fun testCommand() {
            val minecraft = Minecraft.getMinecraft()
            val start = minecraft.thePlayer.position.toLorenzVec()
            val world = minecraft.theWorld
            for (entity in world.loadedEntityList) {
                val position = entity.position
                val vec = position.toLorenzVec()
                val distance = start.distance(vec)
                if (distance < 10) {
                    LorenzDebug.log("found entity: " + entity.name)
                    val displayName = entity.displayName
                    LorenzDebug.log("displayName: $displayName")
                    val simpleName = entity.javaClass.simpleName
                    LorenzDebug.log("simpleName: $simpleName")
                    LorenzDebug.log("vec: $vec")
                    LorenzDebug.log("distance: $distance")

                    val rotationYaw = entity.rotationYaw
                    val rotationPitch = entity.rotationPitch
                    LorenzDebug.log("rotationYaw: $rotationYaw")
                    LorenzDebug.log("rotationPitch: $rotationPitch")

                    if (entity is EntityArmorStand) {
                        LorenzDebug.log("armor stand data:")
                        val headRotation = entity.headRotation.toLorenzVec()
                        val bodyRotation = entity.bodyRotation.toLorenzVec()
                        LorenzDebug.log("headRotation: $headRotation")
                        LorenzDebug.log("bodyRotation: $bodyRotation")

                        /**
                         * xzLen = cos(pitch)
                        x = xzLen * cos(yaw)
                        y = sin(pitch)
                        z = xzLen * sin(-yaw)
                         */

//                        val xzLen = cos(0.0)
//                        val x = xzLen * cos(rotationYaw)
//                        val y = sin(0.0)
//                        val z = xzLen * sin(-rotationYaw)

                        val dir = LorenzVec.getFromYawPitch(rotationYaw.toDouble(), 0.0)

//                        val direction = Vec3(1.0, 1.0, 1.0).rotateYaw(rotationYaw).toLorenzVec()
//                        val direction = LorenzVec(x, y, z)

                        for ((id, stack) in entity.inventory.withIndex()) {
                            LorenzDebug.log("id $id = $stack")
                            if (stack != null) {
                                val cleanName = stack.cleanName()
                                val type = stack.javaClass.name
                                LorenzDebug.log("cleanName: $cleanName")
                                LorenzDebug.log("type: $type")

                            }
                        }
                    }
                    LorenzDebug.log("")
                }
            }
        }
    }

    @SubscribeEvent
    fun renderOverlay(event: RenderGameOverlayEvent.Post) {
        if (!SkyHanniMod.feature.debug.enabled) return

        if (enabled) {
            SkyHanniMod.feature.debug.testPos.renderString(text)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onChatPacket(event: PacketEvent.ReceiveEvent) {
        packetLog.log(event.packet.toString())
    }

//    @SubscribeEvent
//    fun onGetBlockModel(event: RenderBlockInWorldEvent) {
//        if (!LorenzUtils.inSkyblock || !SkyHanniMod.feature.debug.enabled) return
//        val state = event.state
//
//        if (event.state != null && event.pos != null) {
////            if ((event.pos as BlockPos).y <= 76) {
//            val block = (state as IBlockState).block
//
//
//            if (block === Blocks.flowing_lava) {
//                event.state = Blocks.flowing_water.blockState.block.defaultState
//            }
//
//            if (block === Blocks.lava) {
//                event.state = Blocks.water.blockState.block.defaultState
//            }
//
//
//
////            if (block === Blocks.redstone_lamp) {
////                val blockState = Blocks.redstone_lamp.blockState
////                event.state = blockState.block.defaultState
////            }
////                if (block === Blocks.flowing_lava &&
////                    (state as IBlockState).getValue(BlockStainedGlass.COLOR) == EnumDyeColor.WHITE
////                ) {
////                    event.state = state.withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY)
////                }
////                if (block === Blocks.carpet && (state as IBlockState).getValue(BlockCarpet.COLOR) == EnumDyeColor.WHITE) {
////                    event.state = state.withProperty(BlockCarpet.COLOR, EnumDyeColor.GRAY)
////                }
////            }
//        }
//    }
}