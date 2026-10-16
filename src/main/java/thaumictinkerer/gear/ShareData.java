package thaumictinkerer.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShareData(String player, List<String> research) {
    public static final Codec<ShareData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("player").forGetter(ShareData::player),
        Codec.STRING.listOf().fieldOf("research").forGetter(ShareData::research)
    ).apply(instance, ShareData::new));

    public static final StreamCodec<ByteBuf, ShareData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ShareData::player,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ShareData::research,
        ShareData::new
    );
}
