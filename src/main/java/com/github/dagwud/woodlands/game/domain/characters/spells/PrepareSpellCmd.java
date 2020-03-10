package com.github.dagwud.woodlands.game.domain.characters.spells;

import com.github.dagwud.woodlands.game.CommandDelegate;
import com.github.dagwud.woodlands.game.commands.core.AbstractCmd;
import com.github.dagwud.woodlands.game.commands.prerequisites.AbleToActPrerequisite;
import com.github.dagwud.woodlands.game.commands.core.SendMessageCmd;
import com.github.dagwud.woodlands.game.commands.core.ShowMenuCmd;
import com.github.dagwud.woodlands.game.domain.*;

public class PrepareSpellCmd extends AbstractCmd
{
  private static final long serialVersionUID = 1L;

  private final GameCharacter caster;
  private final SingleCastSpell spell;

  public PrepareSpellCmd(GameCharacter caster, SingleCastSpell spell)
  {
    super(new AbleToActPrerequisite(caster));
    this.caster = caster;
    this.spell = spell;
  }

  @Override
  public void execute()
  {
    Encounter activeEncounter = caster.getParty().getActiveEncounter();
    if (activeEncounter == null)
    {
      if (caster instanceof PlayerCharacter)
      {
        CommandDelegate.execute(new SendMessageCmd(((PlayerCharacter) caster).getPlayedBy().getChatId(),
                    "No encounter in progress"));
      }
      return;
    }
    if (activeEncounter.getStatus() == EncounterStatus.FIGHTING)
    {
      CommandDelegate.execute(new SendMessageCmd(character, "Too late - the round has already started"));
      return;
    }
    if (activeEncounter.getStatus() == EncounterStatus.BETWEEN_ROUNDS)
    {
      CommandDelegate.execute(new SendMessageCmd(character, "Hold your horses - the round hasn't started yet"));
      return;
    }
    caster.getSpellAbilities().prepare(spell, activeEncounter.getActionsAllowedPerRound());
    activeEncounter.setHasAnyPlayerActivityPrepared(true);
    if (caster instanceof PlayerCharacter)
    {
      PlayerCharacter character = (PlayerCharacter) caster;
      ShowMenuCmd menu = new ShowMenuCmd(character.getPlayedBy().getPlayerState().getCurrentMenu(), character.getPlayedBy().getPlayerState());
      CommandDelegate.execute(menu);
    }
  }

  @Override
  public String toString()
  {
    return "PrepareSpellCmd{" +
            "caster=" + caster +
            ", spell=" + spell +
            '}';
  }
}
