package components.inventoryTracker

import api.Component
import api.PacketFormation
import api.addPacketInterceptor
import components.commands.Commands
import components.commands.ResponseType
import components.inventoryTracker.models.InventorySession
import components.inventoryTracker.models.Slot
import components.inventoryTracker.packets.client.OpenScreen
import components.inventoryTracker.packets.client.SetContainerContent
import components.inventoryTracker.packets.client.SetContainerSlot
import components.inventoryTracker.packets.client.SetHeldItemClientBound
import components.inventoryTracker.packets.server.ClickContainer
import components.inventoryTracker.packets.server.SetHeldItemServerBound
import components.regions.packets.server.PlayerAction
import models.UserChannel
import packets.client.LoginSuccess
import java.util.*

object InventoryTracker : Component() {
  val inventorySessions = WeakHashMap<UserChannel, InventorySession>()

  override fun enable() {
    addPacketInterceptor(LoginSuccess, PacketFormation.PROXY) { e ->
      inventorySessions[e.userChannel] = InventorySession(e.userChannel)

      e.userChannel.onDisconnect.listen {
        inventorySessions.remove(e.userChannel)
      }
    }

    addPacketInterceptor(SetHeldItemClientBound) { e ->
      inventorySessions[e.userChannel]!!.heldIndex = e.packet.slot!! + 36
    }

    addPacketInterceptor(SetHeldItemServerBound) { e ->
      inventorySessions[e.userChannel]!!.heldIndex = e.packet.slot!!.toInt() + 36
    }

    addPacketInterceptor(SetContainerSlot) { e ->
      if (e.packet.windowId == 0) {
        inventorySessions[e.userChannel]!!.setSlot(e.packet.slotIndex!!.toInt(), e.packet.slot!!.itemStack)
      }
    }

    addPacketInterceptor(ClickContainer) { e ->
      val inventorySession = inventorySessions[e.userChannel]!!
      var offset = 0
      if (e.packet.windowId != 0 && inventorySession.containerOffset != null) {
        offset = inventorySession.containerOffset!!
      }

      e.packet.slots!!.forEach { (slotIndex, slot) ->
        var slotIndex = slotIndex.toInt() - offset
        if (slotIndex < 0) {
          return@forEach
        }

        if (e.packet.windowId != 0) {
          slotIndex += 9
        }

        inventorySession.setSlot(slotIndex, slot.itemStack)
      }
    }

    addPacketInterceptor(OpenScreen) { e ->
      val inventorySession = inventorySessions[e.userChannel]!!
      inventorySession.containerOffset = e.packet.windowType!!.inventoryOffset
    }

    addPacketInterceptor(SetContainerContent) { e ->
      val inventorySession = inventorySessions[e.userChannel]!!
      if (e.packet.windowId != 0) {
        return@addPacketInterceptor
      }

      inventorySession.setSlots(e.packet.slotData!!.map {
        it.itemStack
      })
    }

    addPacketInterceptor(PlayerAction) { e ->
      val inventorySession = inventorySessions[e.userChannel]!!
      if (e.packet.action == PlayerAction.Action.DROP_ITEM) {
        val activeSlot = inventorySession.getSlot(inventorySession.heldIndex!!) ?: return@addPacketInterceptor
        if (activeSlot.quantity > 1) {
          activeSlot.quantity--
        } else {
          inventorySession.setSlot(inventorySession.heldIndex!!, null)
        }
      } else if (e.packet.action == PlayerAction.Action.DROP_ITEM_STACK) {
        inventorySession.setSlot(inventorySession.heldIndex!!, null)
      }
    }

    Commands.register("apply-tracked-inventory") { e ->
      val inventorySession = inventorySessions[e.userChannel]!!
      SetContainerContent(
        windowId = 0,
        slotData = inventorySession.inventory.map {
          Slot(
            isPresent = it != null,
            itemStack = it,
          )
        },
        stateId = 0,
        carriedItem = Slot(
          isPresent = false,
          itemStack = null,
        ),
      ).write(e.userChannel)

      return@register Commands.CommandResponse(ResponseType.SUCCESS, "Applied tracked inventory")
    }
  }

  fun getInventory(userChannel: UserChannel): InventorySession? {
    return inventorySessions[userChannel]
  }
}