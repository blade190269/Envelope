package io.github.mortuusars.envelope.world.item;

import io.github.mortuusars.envelope.Envelope;
import io.github.mortuusars.envelope.world.block.PigeonholeBlock;
import io.github.mortuusars.envelope.world.inventory.PaybackTagMenu;
import io.github.mortuusars.envelope.world.item.component.PaybackRequest;
import io.github.mortuusars.mortaar.Platform;
import io.github.mortuusars.mortaar.world.item.ApplicatorItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class PaybackTagItem extends Item implements ApplicatorItem {
    public PaybackTagItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.ofNullable(stack.get(Envelope.DataComponents.PAYBACK_TAG_CONTENTS));
    }

    @Override
    public boolean canBeAppliedTo(Player player, AbstractContainerMenu menu, Slot slot, ItemStack carried) {
        if (!slot.allowModification(player)) {
            return false;
        }

        if (slot.getItem().is(Envelope.Tags.Items.MAILABLE)) {
            return !Objects.equals(
                  carried.get(Envelope.DataComponents.PAYBACK_TAG_CONTENTS),
                  slot.getItem().get(Envelope.DataComponents.MAIL_PAYBACK_TAG));
        }

        if (slot.getItem().is(Envelope.Items.PAYBACK_TAG.get())) {
            return !Objects.equals(
                  carried.get(Envelope.DataComponents.PAYBACK_TAG_CONTENTS),
                  slot.getItem().get(Envelope.DataComponents.PAYBACK_TAG_CONTENTS));
        }

        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        if (!slot.allowModification(player)) return false;
        ItemStack target = slot.getItem();

        @Nullable PaybackRequest request = stack.get(Envelope.DataComponents.PAYBACK_TAG_CONTENTS);

        if (target.is(Envelope.Tags.Items.MAILABLE)) {
            @Nullable PaybackRequest existingTag = target.get(Envelope.DataComponents.MAIL_PAYBACK_TAG);
            if (request != null) {
                if (stack.getCount() < target.getCount()) {
                    player.playSound(Envelope.SoundEvents.UI_ACTION_BLOCKED.get(), 1, 1);
                    return true;
                }

                if (request.equals(existingTag)) {
                    player.playSound(Envelope.SoundEvents.UI_ACTION_BLOCKED.get(), 1, 1);
                    return true;
                }
                target.set(Envelope.DataComponents.MAIL_PAYBACK_TAG, request);
            } else {
                if (existingTag == null) {
                    player.playSound(Envelope.SoundEvents.UI_ACTION_BLOCKED.get(), 1, 1);
                    return true;
                }
                target.remove(Envelope.DataComponents.MAIL_PAYBACK_TAG);
            }

            slot.setChanged();
            if (request != null && !player.isCreative()) {
                stack.shrink(target.getCount());
            }
            player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1, 1);
            return true;
        }

        // Copy request from carried item to target, but only if requests are different (to not block usual stack splitting behavior)
        if (target.is(Envelope.Items.PAYBACK_TAG.get())
              && !Objects.equals(request, target.get(Envelope.DataComponents.PAYBACK_TAG_CONTENTS))) {
            target.set(Envelope.DataComponents.PAYBACK_TAG_CONTENTS, request);
            player.playSound(Envelope.SoundEvents.UI_WRITE.get(), 1, 1);
            return true;
        }

        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            @Nullable PaybackRequest removedRequest = player.getItemInHand(hand).remove(Envelope.DataComponents.PAYBACK_TAG_CONTENTS);
            if (removedRequest != null) {
                player.playSound(Envelope.SoundEvents.UI_WRITE.get(), 0.35f, 1);
                return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Platform.openMenu(
                  serverPlayer,
                  new SimpleMenuProvider((id, inventory, pl) ->
                        new PaybackTagMenu(id, inventory, hand), Component.translatable("container.envelope.payback_tag")),
                  buffer -> buffer.writeEnum(hand));
            player.getCooldowns().addCooldown(this, 5);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}