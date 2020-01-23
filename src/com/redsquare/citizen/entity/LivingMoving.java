package com.redsquare.citizen.entity;

import com.redsquare.citizen.entity.movement.MovementLogic;
import com.redsquare.citizen.entity.movement.RenderLogic;
import com.redsquare.citizen.item.Inventory;
import com.redsquare.citizen.util.Sets;

import java.util.Set;

public abstract class LivingMoving extends Lifeform {
  private int pickupCheck = 0;

  Sex sex;
  MovementLogic movementLogic;
  Inventory inventory;

  public String getSpriteCode() {
    RenderLogic rl = movementLogic.renderLogic();

    return rl.getDirection().name() + "-" + rl.getPosture().name() + "-" +
            rl.getActivity().name() + "-" + rl.getPoseNum();
  }

  public Sex getSex() {
    return sex;
  }

  private void pickupCheckUpdate() {
    pickupCheck++;
    pickupCheck %= 5;

    if (pickupCheck == 0)
      checkPickup();
  }

  private void checkPickup() {
    Set<Entity> nearbyEntities = position.getAllEntitiesWithinXCells(1);
    Set<Entity> items = Entity.filterEntitySet(
            nearbyEntities, false, false, false, true);

    if (!items.isEmpty()) {
      ItemEntity itemEntity = (ItemEntity) Sets.randomEntry(items);
      boolean delete = inventory.tryPickup(itemEntity);

      if (itemEntity != null && delete)
        itemEntity.position.getWorld().getCell(
                itemEntity.position.world().x, itemEntity.position.world().y).removeEntity(itemEntity);
    }
  }

  @Override
  public void update() {
    super.update();
    movementLogic.update();
    pickupCheckUpdate();
  }
}
