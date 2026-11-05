# TopherTools

### Personal Credits

- Created by KhristopherE.
- Credit to TheWither_MAN for several ideas throughout & testing!
- Credit to Hunterspal for testing!

---

## ⚙️ Requirements & Compatibility

* **Java Version:** Java 25+
* **Server Software:** **Paper** (Tested specifically on Paper, untested elsewhere).
* **Required Plugins:**
    * **[FastAsyncWorldEdit (FAWE)](https://github.com/IntellectualSites/FastAsyncWorldEdit)** `TophersTools` relies
      directly on FAWE's APIs to function.

---

## 🛠️ FastAsyncWorldEdit Brush / Tool Utilities

`{}` = Required Argument, `[]` = Optional Argument

---

### 1. Brush / Tool Macro (`BM`)

* **Description:** Allows creation, loading, and deleting of a set of 1–36 FAWE brush/tools stored as a shareable YAML
  file. It retains original item details, custom names, and inventory slots. Macro names are sanitized for specific
  characters then limited to a maximum length of 48 characters.
* **Commands:** `/brushmacro`, `/bm`, `/toolmacro`, `/tm`
* **Sub-Commands:** `save`, `load`, `delete`

#### Sub-Command Details & Usages:

* **Save Macro**
    * **Usage:** `/{Command} save {macroname} [--override|-o]`
    * **Permission:** `topherstools.bt.macro.save`
    * **Note:**  The `--override` (or `-o`) flag is used to overwrite a macro if it already exists.
* **Load Macro**
    * **Usage:** `/{Command} load {macroname}`
    * **Permission:** `topherstools.bt.macro.load`
* **Delete Macro**
    * **Usage:** `/{Command} delete {macroname} [confirm]`
    * **Permission:** `topherstools.bt.macro.delete`
    * **Note:** Requires a two-step confirmation process with a 15-second window. Running the delete command initially
      registers a pending request; appending `confirm` completes the deletion. Confirm does not tab-complete for this.
* **Help / Info**
    * **Usage:** `/{Command}` (No sub-command)
    * **Permission:** `topherstools.bt.macro`

---

### 2. Get Brush / Tool (`GB`)

* **Description:** Retrieves active or previously bound (after logout or server restart unbound) brush/tool details and
  loadout prompts for yourself, a specified online player & or a specific material (even if the target player is
  offline). Provides clickable chat messages to reapply commands. Supports auto-completion for usernames and stored
  materials so you don't have to guess who has what.
* **Commands:** `/getbrush`, `/gb`, `/gettool`, `/gt`

* **Self Lookup**
    * **Usage:** `/{Command}`
    * **Permission:** `topherstools.bt.get.self`
    * **Behavior:** Inspects the item currently held in your main hand and outputs its bound brush/tool details.
* **Specified Player Lookup**
    * **Usage:** `/{Command} {player}`
    * **Permission:** `topherstools.bt.get.specified.player`
    * **Behavior:** Inspects the main-hand item of the specified online player and outputs the bound brush/tool details.
      Supports tab-completions.
* **Specified Player & Material Lookup**
    * **Usage:** `/{Command} {player} {material}`
    * **Permission:** `topherstools.bt.get.specified.player.specified.material`
    * **Behavior:** Fetches details for a specific material from the target player and outputs the bound brush/tool
      details. (Works even if they are offline).
      Supports tab-completions.

---

### 3. Share / Shout - Brush / Tool (`SB`)

* **Description:** Broadcasts your currently held main-hand brush/tool details to all online players on the server,
  while prompting your own chat with the same brush/tool details.
* **Commands:** `/shoutbrush`, `/shouttool`, `/sharebrush`, `/sharetool`, `/sb`, `/st`
* **Usage:** `/{Command}`
* **Permissions:** `topherstools.bt.shout` **& OR** `topherstools.bt.share`

---

## 4. Return All Brush / Tools (`RAT`)

* **Description:** Fetches up to 36 saved brush/tool loadouts for the requesting player and rebinds/returns
  them to inventory slots 0–35. (Messy, should really just use the Brush Macro lol, but if it works... it works).
* **Commands:** `/returnallbrushes`, `/rab`, `/returnalltools`, `/rat`
* **Usage:** `/{Command}`
* **Permission:** `topherstools.bt.returnall`
* **⚠️ Warning:** This will clears inventory slots `0–35` prior to applying loadouts.

---

## 5. Clear All Brush / Tools (`CAT`)

* **Description:** Clears and unbinds all active brush/tool loadouts from the database for the requesting player and
  strips the tool bindings from FastAsyncWorldEdit entirely.
* **Commands:** `/clearallbrushes`, `/cab`, `/clearalltools`, `/cat`
* **Usage:** `/{Command}`
* **Permission:** `topherstools.bt.clearall`

---

## 6. Reload Configuration

* **Description:** Reloads all configuration settings (except SQL Settings, these reload on restart) and cooldown
  configurations for the plugin's brush-tool
  utilities.
* **Commands:** `/topherstools reload btutils`, `/tt r btu`
* **Usage:** `/{Command}`
* **Permission:** `topherstools.reloadconfig.fawebtutils`

---

## 📦 Dependencies & Acknowledgments

### Shaded / Embedded Libraries - Credits

The following libraries are bundled directly inside the `TophersTools` compiled binary (`.jar`):

- **[HikariCP](https://github.com/brettwooldridge/HikariCP)** (`com.zaxxer:HikariCP`)
    - **License:** [Apache License 2.0](https://github.com/brettwooldridge/HikariCP/blob/dev/LICENSE)
    - **Copyright:** (C) 2013, 2014 Brett Wooldridge


- **[Google Gson](https://github.com/google/gson)** (`com.google.code.gson:gson`)
    - **License:** [Apache License 2.0](https://github.com/google/gson/blob/main/LICENSE)


- **[Cloud](https://github.com/Incendo/cloud)** (`org.incendo:cloud-paper`, `org.incendo:cloud-annotations`)
    - **License:** [MIT License](https://github.com/Incendo/cloud/blob/master/LICENSE)

> *For full license texts and copyright notices of bundled dependencies, please refer to the
bundled [THIRD-PARTY-LICENSES.md](THIRD-PARTY-LICENSES.md) file.*

---

### Compile-Only Dependencies - Credits

* **[Paper API](https://papermc.io/)**, licensed under
  the [GNU General Public License v3.0](https://github.com/PaperMC/Paper/blob/main/LICENSE.md).

* **[FastAsyncWorldEdit](https://github.com/IntellectualSites/FastAsyncWorldEdit)**, licensed under
  the [GNU General Public License v3.0](https://github.com/IntellectualSites/FastAsyncWorldEdit/blob/main/LICENSE.txt).

* **[WorldEdit](https://enginehub.org/worldedit/)**, licensed under
  the [GNU General Public License v3.0](https://github.com/EngineHub/WorldEdit/blob/master/LICENSE.txt).

---

## License

This project is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE).  
For third-party legal notices regarding embedded components, see [THIRD-PARTY-LICENSES.md](THIRD-PARTY-LICENSES.md)