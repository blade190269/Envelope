package io.github.mortuusars.envelope.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public record LetterContent(Component text) {
    public static final Codec<LetterContent> CODEC = RecordCodecBuilder.create(i -> i.group(
          ComponentSerialization.CODEC.optionalFieldOf("text", CommonComponents.EMPTY).forGetter(LetterContent::text)
    ).apply(i, LetterContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LetterContent> STREAM_CODEC = StreamCodec.composite(
          ComponentSerialization.STREAM_CODEC, LetterContent::text,
          LetterContent::new
    );

    public static final LetterContent EMPTY = new LetterContent(CommonComponents.EMPTY);

    public boolean isEmpty() {
        return this.equals(EMPTY) || text().getString().isEmpty();
    }
}