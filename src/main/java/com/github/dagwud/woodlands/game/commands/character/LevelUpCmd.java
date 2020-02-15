package com.github.dagwud.woodlands.game.commands.character;

import com.github.dagwud.woodlands.game.CommandDelegate;
import com.github.dagwud.woodlands.game.commands.RecoverHitPointsCmd;
import com.github.dagwud.woodlands.game.commands.RecoverManaCmd;
import com.github.dagwud.woodlands.game.commands.character.level.ILevelUpAward;
import com.github.dagwud.woodlands.game.commands.core.AbstractCmd;
import com.github.dagwud.woodlands.game.commands.core.DiceRollCmd;
import com.github.dagwud.woodlands.game.commands.core.SendMessageCmd;
import com.github.dagwud.woodlands.game.commands.core.SendPartyMessageCmd;
import com.github.dagwud.woodlands.game.commands.inventory.SpawnTrinketCmd;
import com.github.dagwud.woodlands.game.domain.PlayerCharacter;
import com.github.dagwud.woodlands.game.domain.trinkets.AmuletOfProtection;
import com.github.dagwud.woodlands.game.domain.trinkets.WardOfViolence;

import java.util.HashMap;
import java.util.Map;

public class LevelUpCmd extends AbstractCmd
{
  private static final long serialVersionUID = 1L;
  private static final Map<Integer, ILevelUpAward> AWARDS = new HashMap<Integer, ILevelUpAward>()
  {{
    put(8, character1 -> new SpawnTrinketCmd(character1, new AmuletOfProtection()).go());
    put(7, character1 -> new SpawnTrinketCmd(character1, new WardOfViolence()).go());
  }};

  private final int chatId;
  private final PlayerCharacter character;

  public LevelUpCmd(int chatId, PlayerCharacter character)
  {
    this.chatId = chatId;
    this.character = character;
  }

  @Override
  public void execute()
  {
    int hitPointsGained = increaseHitPoints();
    int manaGained = increaseMana();
    character.getStats().setRestPointsMax(character.getStats().getRestPointsMax() + 1);
    character.getStats().setRestPoints(character.getStats().getRestPoints() + 1);

    SendPartyMessageCmd msgParty = new SendPartyMessageCmd(character.getParty(), "🍾 " + character.getName() + " has levelled up!");
    CommandDelegate.execute(msgParty);

    AbstractCmd msg = new SendMessageCmd(chatId, "🍾 You have levelled up! Hit Point boost: ❤" + hitPointsGained + (manaGained != 0 ? ", Mana boost: ✨" + manaGained : ""));
    CommandDelegate.execute(msg);

    int newLevel = character.getStats().getLevel() + 1;

    if (AWARDS.containsKey(newLevel))
    {
      AWARDS.get(newLevel).award(character);
    }

    character.getStats().setLevel(newLevel);
  }

  private int increaseHitPoints()
  {
    DiceRollCmd roll = new DiceRollCmd(1, character.getStats().getRestDiceFace());
    CommandDelegate.execute(roll);
    int hpToGain = roll.getTotal() + character.getStats().getConstitutionModifier();
    character.getStats().getMaxHitPoints().setBase(character.getStats().getMaxHitPoints().getBase() + hpToGain);

    RecoverHitPointsCmd hpCmd = new RecoverHitPointsCmd(character, hpToGain);
    CommandDelegate.execute(hpCmd);
    return hpToGain;
  }

  private int increaseMana()
  {
    int initialMana = character.getStats().getMaxMana().getBase();
    int totalMana = 3 + Math.floorDiv(character.getStats().getLevel(), 4);
    if (totalMana > character.getStats().getMaxMana().getBase())
    {
      RecoverManaCmd cmd = new RecoverManaCmd(character, 1);
      CommandDelegate.execute(cmd);
    }
    character.getStats().getMaxMana().setBase(totalMana);
    return totalMana - initialMana;
  }
}
