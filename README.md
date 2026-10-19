# TopherTools
Credits
- Created by KhristopherE.
- Credit to TheWither_MAN for many ideas throughout and sparking the seed for this project & helping test the functionalities.
- Credit to Hunterspal for being someone to talk to about this project & helping test the functionalities.

The current functionalities
- FAWE Brush/Tool Utilities (Depends on FAWE being installed of course) {} = Required, [] Optional.
  - Brush/Tool Macro
    - Description: Allows creation, loading, & deleting of a set of 1-36 premade FAWE Brush/Tools to be loaded to a users inventory. Retaining the original item details, custom names etc... & the inventory slot it was in at the time of saving. It will only save items that it believes are brushes/tools and are actively saved in the Database as well.
      - Commands: /BrushMacro, /BM, /ToolMacro, /TM.
      - Sub Commands, Save, Load, Delete
      - Note: MacroName has a Limitation of 48 characters.
      - Usage, "/{Command} [Sub Command] [MacroName]"
      - Permissions
        - Save - "topherstools.bt.macro.save"
        - Load - "topherstools.bt.macro.load"
        - Delete - "topherstools.bt.macro.delete"
        - Help (/{Command}, No Sub Command) -  "topherstools.bt.macro"
  - Get Brush/Tool
    - Description: Gather's the requested players brush/tool & details, provides a clickable chat command to reapply the brush/tool commands to a different item, or provide a cloned item with the same commands. Can be utilized in the event FAWE no longer recognized the brush/tool to call back and reapply the same commands. Best utilized if you disconnected for some time and the brush/tool details did not retain in FAWE.
      - Commands: /GetBrush, /GB, /GetTool, /GT
      - Usage, "/{Command} [PlayerName] [Item/Material Name]"
      - Permissions/Sub Description
        - {Command} (For Self Usage) - "topherstools.bt.get.self"
          - Provides the brush/tool details for the currently held item of the requester.
        - {Command} [PlayerName] - "topherstools.bt.get.specified.player"
          - Provides the brush/tool details for the currently held item of the player name requested.
          - Tab Complete lists all players in the Database that have bound brush/tools saved.
        - {Command} [PlayerName] [Item/Material Name] - "topherstools.bt.get.specified.player.specified.material"
          - Provides the brush/tool details for the specified item requested of the player name requested. (Functions when the player is offline).
          - Tab Complete lists all items/materials in the Database for this player.
  - Share/Shout Brush/Tool
    - Description: The same as Get Brush/Tool although, only the /{Command} is the only option, but it also shares the details to all players online.
      - Commands: /ShoutBrush, /ShoutTool, /ShareBrush, /ShareTool, /SB, /ST
      - Usage, "/{Command}"
      - Permissions, "topherstools.bt.shout", & OR "topherstools.bt.share"
  - Return All Brush/Tools (RAT)
    - Description: Returns All Brush/Tools in the Database for this player back to the requesting player. WARNING: This will clear your inventory slots 0-35 & there is no limit to the number of Brush/Tools it will return all of them, if there is no room, you will start dropping items.
      - Commands: /ReturnAllBrushes, /RAB, /ReturnAllTools, /RAT. I much prefer RAT.
      - Usage, "/{Command}"
      - Permission, "topherstools.bt.returnall"
  - Clear All Brush/Tools (CAT)
    - Description: Clears/Unbinds All Brush/Tools in the Database for this player & unbinds them in FAWE.
      - Commands: /ClearAllBrushes, /CAB, /ClearAllTools, /CAT. I much prefer CAT.
      - Usage, "/{Command}"
      - Permission, "topherstools.bt.clearall"

## 📜 Credits & Third-Party Dependencies

TophersTools relies on several amazing open-source projects. To comply with open-source licensing standards, dependencies are categorized by how they are integrated into the project below.

### 📦 Shaded / Embedded Libraries
The following libraries are bundled directly inside the `TophersTools` compiled binary (`.jar`):

* **[Cloud Command Framework](https://github.com/Incendo/cloud-minecraft)** (`org.incendo:cloud-paper`, `org.incendo:cloud-annotations`)
  * **License:** [MIT License](https://github.com/Incendo/cloud-minecraft/blob/master/LICENSE)
  * **Copyright:** © Alexander Söderberg / Incendo Team
  * Modern command framework and annotation parser used for command handling.

* **[Google Gson](https://github.com/google/gson)** (`com.google.code.gson:gson`)
  * **License:** [Apache License 2.0](https://github.com/google/gson/blob/main/LICENSE)
  * **Copyright:** © Google LLC
  * Java serialization/deserialization library used for JSON data handling.

* **[Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)** (`org.apache.commons:commons-lang3`)
  * **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
  * **Copyright:** © The Apache Software Foundation
  * Core Java utility helper classes (included as a dependency constraint).

---

### 🌐 Provided / Compile-Only Dependencies
The following dependencies are **not** bundled inside the plugin JAR. They are provided at runtime by the server software or prerequisite server plugins:

* **[Paper API](https://papermc.io/)** (`io.papermc.paper:paper-api`)
  * **License:** [GPLv3](https://github.com/PaperMC/Paper/blob/main/LICENSE.md) / [MIT](https://github.com/PaperMC/Paper/blob/main/LICENSE.md)
  * High-performance Minecraft server software API.

* **[FastAsyncWorldEdit (FAWE)](https://github.com/IntellectualSites/FastAsyncWorldEdit)** (`com.fastasyncworldedit:FastAsyncWorldEdit-Core`)
  * **License:** [GPLv3](https://github.com/IntellectualSites/FastAsyncWorldEdit/blob/main/LICENSE.txt)
  * Fast async building and manipulation framework.

* **[WorldEdit](https://github.com/EngineHub/WorldEdit)** (`com.sk89q.worldedit:worldedit-bukkit`)
  * **License:** [GPLv3](https://github.com/EngineHub/WorldEdit/blob/master/LICENSE.txt)
  * In-game Minecraft map editor.

* **[IntellectualSites BOM](https://github.com/IntellectualSites/bom)** (`com.intellectualsites.bom:bom-newest`)
  * **License:** [MIT License](https://github.com/IntellectualSites/bom/blob/main/LICENSE)
  * Bill of Materials for aligning IntellectualSites dependency versions during build time.

---

## 📄 License

This project is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE).  
For third-party legal notices regarding embedded components, see [THIRD-PARTY-LICENSES.md](THIRD-PARTY-LICENSES.md)