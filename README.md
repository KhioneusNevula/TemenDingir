<h1>TEMEN-DINGIR</h1>
Sumerian: "Foundations of the Gods"
<br>
Mod which adds a religion faction system alongside magic powers from the gods
<br>
<h2>Gods</h2>
Gods are sentient constructs created through an act of worship. Any person can create one at any time or pledge themself to a god; a player can only have one god. The creator of a god is automatically its High Priest.<br>
To create a god, one must construct an altar of sorts. This altar is four wood stair blocks facing in cardinal directions with a water block in their center. A fence must be placed in the center of the water, and atop this fence a torch must be placed. Upon right clicking the torch with a stick, harmless lightning will strike it, destroying the torch and turning the fence and water into the primordial blue-green water of Apsu and Tiamat. This primordial water will vanish if the stair blocks are removed. Additionally, a special book-and-quill will be given to the player. The first line written in the book and quill will be the name of the god. The book and quill must be tossed into the primordial water.
After that, four items must be tossed in--these items will represent the "quadrinity" that invokes the deity and cannot be identical to those of another deity (if identical, the last item will be tossed back out). Note that NBT data will be ignored, i.e. a banner tossed in will prime the Invocation to sense ALL banners, not just banners with the given pattern. Once the items are tossed in (more than one item can be identical; order doesn't matter; the items will be consumed) the primordial water will vanish.<br>
After this, a Rudimentary god will be born. A Rudimentary is an embryonic god who is not fully born until it is Defined. To Define a god, a Creation Altar must be built, one which is composed of three stone blocks side by side with one raised, and each one having an item frame on one side with an invoker item. The top of each block must have a torch. Once this is complete, right click any block in the altar to bring up a specification GUI. Only the High Priest can do this. It is very important to note that adding conflicting commandments/worship methods/etc is perfectly possible, but highly unwise.
<br>
<h3>Specification GUI</h3>
The rudimentary god's altar will bring up a gui to specify its religion.
<h4>Pronouns</h4>
These are he/him, she/her, or they/them, and determine the god's form of address
<h4>Commandments</h4>
Commandments are prohibitions in a religion. They follow a system known as "commandment points." Commandments are rules that the faithful follow; to break one is to lose some favor with the god, and for a priest to break one is to lose priesthood. Each commandment is worth a certain amount of points; a minimum of seven commandment points need to be expended, but some commandments are worth more than others since they are more difficult to uphold but also lose less favor if they are broken. A High Priest can add as many commandments to their god as they want; leveling up the god lowers the minimum commandment amount.
<ul>
  <li>No killing passive or neutral mobs (2 pt): Killing a passive mob (directly) loses favor.
    <li>No killing hostile mobs (4 pt): Killing a hostile mob (directly) loses favor.
      <li>Love one another (2 pt): Killing a player of any sort loses favor.
      <li>Love your neighbor (1 pt): Killing another of the same religion loses favor.
      <li>No potioncraft (1 pt): Drinking a potion (willingly) loses favor; being hit by one does not.
        <li>No enchanting (3 pt): Clicking with an enchanted tool or equipping enchanted armor loses favor; for armor, only the initial equip loses favor. 
          <li>No cursing (1 pt): Applying a curse to an item loses favor
            <li>No wearing chestgear (5 pt): Equipping a chestgear item causes a loss in favor
              <li>No eating meat (2 pt): Eating a meat consumable that is NOT fish causes a loss in favor.
                <li>No eating crops (3 pt): Eating a plant consumable causes a loss in favor
                  <li>No false idols (1 pt): Construction of an iron or snow golem will cause a loss in favor.
                    <li>No sleep (1 pt): Sleeping in a bed for more than a 30 seconds or skipping the night will cause a loss in favor.
                    </ul>
<h4>Worship Methods</h4>
Similar to commandments, worship system has something called "worship points." Worship is the actions taken at an altar that empower one's god to raise its level and powers; there can only be a maximum of 7 worship points expended (leveling can increase that) with a minimum of 1 and more difficult actions are worth less worship points (to facilitate the variety in worshiping methods) but also give more power to the god. Worship is performed through some entity or item being put in a sacred fire or, if an entity, being killed with a Sacred Fire weapon. 
<ul>
  <li>Human sacrifice (1 pt): Sacrifice a villager to the sacred fire, with a higher level and stock being worth more.
    <li>Hostile sacrifice (1 pt): Sacrifice a hostile mob to the sacred fire, with higher health worth more. Player sacrifice is also allowed under this.
      <li>Passive sacrifice (2 pt): Sacrifice an animal to the sacred fire, with higher health worth more.
        <li>Tool sacrifice (3 pt): Sacrifice tools to the sacred fire, with higher tiers and durability worth more.
          <li>Food sacrifice (5 pt): Sacrifice a consumable to the sacred fire, with higher food points worth more.
            <li>Plant sacrifice (6 pt): Sacrifice plant items (flowers, saplings, cacti, vines, logs, and seeds) to the sacred fire.
              <li>Wealth sacrifice (3 pt): Sacrifice ores (except coal); more rarity is worth more.
</ul>
<h4>Worship Conditions</h4>
A minimum of 4 worship condition points must be expended. Worship conditions are conditions that must be met in order for worship to occur successfully. If not, the worship will fail. More difficultly met conditions are worth more worship condition points. Almost all conditions are going to be under the player's control.
<ul>
  <li>Worship in day (2): Worship must occur during the daytime.
    <li>Worship in night (2): Worship must occur during night. (cancels out #1)
      <li>Worship in a building (2): Worship must occur in an enclosed building (an enclosed chamber where all collidable blocks can count as walls including doors)
        <li>Worship in a library (1): Worship must occur with at least 4 bookshelves less than seven blocks away.
          <li>Worship in <non-Overworld dimension> (3): Worship must occur in the given dimension (might cancel out #1 or #2)
</ul>
            <h4>Worship Modifiers</h4>
  Worship modifiers are conditions that increase the amount of worship points gotten from a worship. A maximum of 4 points can be expended, a minimum of zero, with more difficult-to-achieve conditions being worth less points.
  <ul>
  <li>Nature (3 pt): each nearby plant block (log, leaf, sapling, flower, flowerpot, etc) adds some percent of additional worship (maximum radius is twenty)
    <li>Library (3 pt): Each nearby bookshelf adds some percent of additional worship (if Worship in Library is selected, each bookshelf over the number 4; maximum radius is twenty)
      <li>Flame (2 pt): Each nearby fire/soulfire/lava/campfire block adds some percent of additional worship. (maximum radius is twenty)
        <li>Water (2 pt): Each nearby water block adds some percent of additional worship (maximum radius is twenty)
          <li>Art (1 pt): Each nearby painting, wool block, and banner (depending on size) and item frame with an item adds some percent of worship
          <li>Decoration (1 pt): Each nearby armor stand with armor (points additional depending on tool/armor tier), block of metal/gem, item frame with ingot/gem//tool/similar item
</ul>
            <h4>Mandates</h4>
Mandates are actions that can be taken to increase a deity's favor. (Be wary that they can cancel out commandments, rendering a deity's favor ineffective) More difficult mandates are worth less mandate points but give more favor; a maximum of seven mandates can exist.
<ul>
  <li>Default mandate: Worship. By default, attending a worship gives you favor with a deity.
<li>Kill nonbelievers (4 pt): Kill players and villagers who do not follow your religion
  <li>Convert nonbelievers (4 pt): Convert villagers to your religion
    <li>Holy lands (4 pt): Increase the area of your deity's holy land.
    <li>Kill the undead (5 pt): Kill undead mobs
      <li>Kill the passive (6 pt): Kill all passive mobs.
        <li>Kill the neutral (3 pt): Kill mobs such as endermen and zombie pigmen who are neutral
          <li>Kill the higher beasts (1 pt): Kill evokers, elder guardians, wither, ender dragons, and other 'renewable' boss mobs
            <li>Protect the people (1 pt): Become a Hero of the Village
              <li>Protect the helpless (5 pt): Kill a creature that is targetting a passive mob or villager and is not in a Raid
              <li>(?) Give generously (4 pt): You will have the option of trading with villagers where the villager can take your side of the trade for free; doing this increases favor (?)
                <li>
  <li>
</ul>
            <h4>Enemies</h4>
Enemies are the mobs that this deity's power wards against (i.e. they take damage on the deity's holy land and sacred magic does much more damage to them). A maximum of 5 points can be expended. Each enemy species is worth 1 points and can be all non-boss hostile or neutral mobs. "Nonbelievers" can be chosen as one group but are worth 2 points.
            <h4>Relationship Trait</h4>
This is a concept that only makes sense on a multiplayer server, and determines how the god gets along with other gods. WARLIKE causes the god to frequently incite holy war missions to kill unbelievers. INVASIVE causes the god to incite peaceful missions to convert unbelievers. PEACEFUL causes the god's missions to be able to be done without interaction with other gods, and this is the default. FRIENDLY causes the god to attempt to ally with other gods to create a (?)Syncretism(?).
            <h4>Blessings</h4>
            Blessings are passive effects that have a chance of being applied to a worshiper to help them. This 'help' is usually not combative; it is based on defending, protecting, and restoring a worshiper and their possessions. Blessings' chance of working on a worshiper is dependent on a worshiper's favor with their deity; low favor has a 5% chance, medium has a 10%, high 20%, really high 40%, super duper high 50%, extremely high 75%, ultimate devotee 85%. Blessing points have a max of 7 and a min of 0. 
            <br>Each blessing randomly becomes active depending on the god's mood, and the time intervals between these active states are generally lesser if the player has more favor with the god. When the random fluctuation is about to set to an active state, the icon representing the blessing in the HUD will display a timer of 7 seconds. Then, the blessing will be active for twenty seconds; during this time it will apply its effect to the first instace of its situation it recognizes and immediately deactivate after that (or it will time out after twenty seconds) and then will be inactive until its god's mood reactivates it.
            <ul>
              <li>Regeneration 2, 4, 6: Regeneration is applied to a worshiper if their health is beneath 2, 4, 6 (depending on which is chosen) hearts. If regeneration is active, blessing will not become active.
                <li>Protection: Protection is applied for twenty seconds if worshiper is attacked. If protection is active, blessing will not become active.
                <li>Armor Repairment: When the worshiper is attacked, the worshiper's armor is repaired by the amount of attack dealt to it instead of being damaged.
                  <li>Tool Repairment at 10%, 20%, 30%: A worshiper's tools in inventory will all be repaired up to half durability if lower than the given durability upon being used
                    <li>Invisibility: If a worshiper is within 16 blocks of a hostile, they will turn invisible for a minute. If invisibility is active, blessing will not become active.
                      <li>Fire Protection: If a worshiper is on fire, they will gain fire protection for ten seconds. If fire protection is active, blessing will not become active.
                        <li>Water Breathing: If a worshiper is in water, they will gain water breathing for a minute. If water breathing is active, blessing will not become active.
                          <li>Night Vision: If a worshiper is in a place of brightness less than 4, they will gain night vision for two minutes. If night vision is active, blessing will not become active.
                            <li>Let There Be Light: If a worshiper is in a place of brightness less than 8, they get the Glowing Head status effect, causing their head to emit light for two minutes. If glowing head is active, blessing will not become active.
                              <li>Wealth: If a worshiper is about to trade with a villager, all of the villager's trades that are more expensive than the worshiper's current amount of emeralds will be waived so that they consumes all the worshiper's emeralds instead of being unobtainable. This effect will remain on that specific villager (only applying to the worshiper) for a minute.
                                <li>Saturation: If a worshiper eats any food they temporarily get saturation for twenty seconds and an extra amount of food. If saturation is active, same deal as above.
                                  <li>Fall Protection: If a worshiper falls a great height, their fall damage will be completely negated. Additionally, if a worshiper is about to fall into the void, they will get the levitation status effect for ten seconds.
                                    <li>Projectile Blink: If a projectile is about to hit the worshiper, it will simply fall from the air.
                                      <li>Divine Retaliation: If an entity attacks the worshiper it will be struck by lightning.
                                        <li>
            </ul>
            
 <br>           Upon finishing the specification of the god, the Create button must be pressed, after which the altar will be struck with a lightning blast. Once this is complete, the altar, if right clicked by anyone, will show a gui which shows a single slot with an arrow to another slot; if a book or book-quill is placed in the left slot a written book--a "holy book"--will appear in the right slot. This is a dynamic book which changes depending on the god's current tendencies and state. It will list all the information used in Specification. For the High Priest, the altar of Creation will have two buttons when right clicked: 'Specifications' and 'Holy Book'. The holy book button will go to the holy book gui, and the specifications button will go to the specification editing menu. When a specification is edited, the "Change" button can be pressed, and a message will be sent to all believers that the god now has that specification.
<br>==========================================================================================================<br>
            After you've specified the god's tendencies, you should get to worshiping! Remember, don't break the tenets, and try to get favor with your deity.
            <h3>Worship</h3>
            Worship is a process through which a god gains power via a Sacred Fire. A god's energy increases with each thing given as worship. Energy is accessed through Energy Altars. An energy altar can power godly machines which are used to, e.g., forge godly weapons or use godly automation. Additionally, a godly tool's power recharge rate is also determined by the amount of worship. 
            <h4>Worship Altar</h4>
           <br>A worship altar is built using a three-by-three stone structure, where the center block of each side is raised in an away-facing stairs block and an item frame on its away side (below the stair block) containing an item from the god's quadrinity. This marks the altar as belonging to this SPECIFIC god as opposed to any other god. Then, a torch must be placed on each spot around the item frames on the sides of the blocks, and finally, a fire must be lit inside. The fire will then turn into a Sacred Fire, (which vanishes if the surrounding altar structure is disfigured or the block it is burning on is destroyed) and it will rise about three blocks so it protrudes from the top of the altar. Any valid sacrificial item or entity within two blocks of the fire will be sucked into it and it will be bound in place; if it is an entity its health will be sucked away quickly. Even a boss such as the wither can be bound in this manner. A worship altar keeps ownership of some of the god's worship before giving it to the god overtime; the god is still able to access this worship, HOWEVER, if the altar is destroyed this means that some amount of worship energy the god has is actually <em>lost</em>.
            <h4>Energy Altar</h4>
            It is crafted using stone brick stairs facing away from the center, an upward-facing stone pillar on top of another pillar, and atop the higher one is a redstone lamp which has an invocative item frame on each face. Atop the redstone lamp a fire must be lit. This fire will become a Conduit Fire, where the redstone lamp will be on while the altar has energy in it, and the stone brick stairs will start emitting Divine Energy in the direction they're facing, which can be sent to godly machines. An energy altar only emits energy when a machine requests energy from it; right clicking the middle stone pillar will allow a user to change its power output from auto to manual and manually manipulate its power output for each side or turn it off. A redstone signal to this central pillar will turn it off automatically.
            
