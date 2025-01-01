# VCMaker
 A private-adjacent voice channel creator

---

## Features

- Users with Administrator permission can select which text channels the commands should be able to be used in
  - The text channel must be within a category

- Users can create voice channels with names and optional max user count
- The voice channel created automatically sets @everyone connect permission to deny, and sets the creator's connect permission to allow
- Only the creator can use the set-permission command to allow and deny permissions for other users to connect
- Only the creator can remove the voice channel
- Voice channels delete themselves 10 seconds after the last user disconnects

---

## Commands

- /commands <True/False>
  - Administrator only. Enabled/disables all other commands from being usable in the text channel that this command is issued in.

- /create <channel_name> [max_users]
  - Creates a voice channel with a required name and an optional user limit.

- /set-permission <channel_name> <user> <True/False>
  - Allows or denies the specified person from being able to join the specified voice channel. Only the creator of the voice channel can do this!
 
- /remove <channel_name>
  - Removes the voice channel with the specified name. Only the creator of the voice channel can do this!
