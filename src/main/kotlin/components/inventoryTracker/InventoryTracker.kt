package components.inventoryTracker

import api.Component
import api.addPacketInterceptor
import components.inventoryTracker.packets.client.SetContainerSlot
import components.inventoryTracker.packets.client.SetHeldItemClientBound
import components.inventoryTracker.packets.server.ClickContainer
import components.inventoryTracker.packets.server.SetHeldItemServerBound

object InventoryTracker : Component() {
  override fun enable() {
    addPacketInterceptor(SetHeldItemClientBound) { e ->
      println("C SetHeldItem: ${e.packet.slot}")
    }

    addPacketInterceptor(SetHeldItemServerBound) { e ->
      println("S SetHeldItem: ${e.packet.slot}")
    }

    addPacketInterceptor(SetContainerSlot) { e ->
      println("SetContainerSlot: ${e.packet}")
    }

    addPacketInterceptor(ClickContainer) { e ->
      println("ClickContainer: ${e.packet}")
    }
  }
}