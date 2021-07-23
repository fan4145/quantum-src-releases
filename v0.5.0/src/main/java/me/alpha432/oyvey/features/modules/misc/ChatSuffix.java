  package me.alpha432.oyvey.features.modules.misc;

  import me.alpha432.oyvey.OyVey;
  import me.alpha432.oyvey.event.events.PacketEvent;
  import me.alpha432.oyvey.features.modules.Module;
  import me.alpha432.oyvey.features.setting.Setting;
  import net.minecraft.network.play.client.CPacketChatMessage;
  import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

  public class ChatSuffix extends Module {
    public Boolean suffix = true;
    public String s;

    public ChatSuffix() {
        super("ChatSuffix", "flexs your quantum continued supremacy.", Module.Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
      if ( event.getStage ( ) == 0 && event.getPacket ( ) instanceof CPacketChatMessage ) {
        CPacketChatMessage packet = event.getPacket ( );
        String s = packet.getMessage ( );
        if ( s.startsWith ( "/" ) ) {
          return;
        }
        s = s + " \u23d0 \uff51\uff55\uff41\uff4e\uff54\uff55\uff4d\u3000 \uff43\uff4f\uff4e\uff54\uff49\uff4e\uff55\uff45\uff44";
        if ( s.length ( ) >= 256 ) {
          s = s.substring ( 0 , 256 );
        }
        packet.message = s;
      }
    }

    @SubscribeEvent
    public
    void onChatPacketReceive ( PacketEvent.Receive event ) {
        if ( event.getStage ( ) == 0 ) {
            event.getPacket ( );
        }// empty if block
    }
  }
