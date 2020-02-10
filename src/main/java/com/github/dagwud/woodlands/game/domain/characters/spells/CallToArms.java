package com.github.dagwud.woodlands.game.domain.characters.spells;

import com.github.dagwud.woodlands.game.CommandDelegate;
import com.github.dagwud.woodlands.game.commands.core.DiceRollCmd;
import com.github.dagwud.woodlands.game.commands.core.SendMessageCmd;
import com.github.dagwud.woodlands.game.domain.GameCharacter;
import com.github.dagwud.woodlands.game.domain.ECharacterClass;
import com.github.dagwud.woodlands.game.domain.PlayerCharacter;
import com.github.dagwud.woodlands.game.domain.characters.General;

import java.util.HashMap;
import java.util.Map;

public class CallToArms extends SingleCastSpell
{
  private static final long serialVersionUID = 1L;

  private Map<GameCharacter, Integer> buffs;

  public CallToArms(General caster)
  {
    super("Call to Arms", caster);
    buffs = new HashMap<>();
  }

  @Override
  public boolean cast()
  {
    for (GameCharacter target : getCaster().getParty().getActiveMembers())
    {
      int buffAmount = rollBuff(target);
      if (buffAmount != 0)
      {
        target.getStats().getStrength().addBonus(buffAmount);
        target.getStats().getAgility().addBonus(buffAmount);
        buffs.put(target, buffAmount);

        if (target instanceof PlayerCharacter)
        {
          SendMessageCmd cmd = new SendMessageCmd(((PlayerCharacter) target).getPlayedBy().getChatId(),
                  getCaster().getName() + " buffed your strength and agility by +" + buffAmount);
          CommandDelegate.execute(cmd);
        }
      }
    }
    return true;
  }

  @Override
  public void expire()
  {
    for (GameCharacter target : buffs.keySet())
    {
      Integer buffedAmount = buffs.get(target);
      target.getStats().getStrength().removeBonus(buffedAmount);
      target.getStats().getAgility().removeBonus(buffedAmount);
    }
  }

  private int rollBuff(GameCharacter target)
  {
    if (getCaster() == target)
    {
      // Can't buff yourself:
      return 0;
    }

    int diceFaces = determineBuffDiceFaces(target);

    DiceRollCmd roll = new DiceRollCmd(1, diceFaces);
    CommandDelegate.execute(roll);

    return roll.getTotal();
  }

  private int determineBuffDiceFaces(GameCharacter targetChar)
  {
    if (!(targetChar instanceof PlayerCharacter))
    {
      // NPCs don't get buffed:
      return 0;
    }
    PlayerCharacter target = (PlayerCharacter) targetChar;
    if (target.getCharacterClass() == ECharacterClass.TRICKSTER)
    {
      return 6;
    }
    if (target.getCharacterClass() == ECharacterClass.DRUID
            || target.getCharacterClass() == ECharacterClass.WIZARD
            || target.getCharacterClass() == ECharacterClass.EXPLORER)
    {
      return 4;
    }
    if (target.getCharacterClass() == ECharacterClass.BRAWLER)
    {
      return 8;
    }
    if (target.getCharacterClass() == ECharacterClass.GENERAL)
    {
      // Generals can't buff each other
      return 0;
    }
    return 0;
  }

  @Override
  public General getCaster()
  {
    return (General) super.getCaster();
  }

}
