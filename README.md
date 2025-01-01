# VCMaker
 A private-adjacent voice channel creator for Discord

---

## Features

- Users with Administrator permission can select which text channels the commands should be able to be used in
  - The text channel must be within a category

- Users can create voice channels with a name, optional max user count and optional delay
  - The voice channel is always created within the same category as the text channel the command was issued from
  - The voice channel prepends your username and appends a count if more than 1 of the base channel name exists
- The voice channel created automatically sets @everyone connect permission to deny, and sets the creator's connect permission to allow
- Only the creator can use the set-permission command to allow and deny permissions for other users to connect
- Only the creator can remove the voice channel
  - Administrators are an exception to this
- Voice channels delete themselves 10 seconds after the last user disconnects

---

## Commands

- /commands <True/False>
  - Administrator only. Enabled/disables all other commands from being usable in the text channel that this command is issued in.

- /create <channel_name> [max_users] [delay]
  - Creates a voice channel with a required name, an optional user limit and an optional delay on creation, measured in minutes.

- /set-permission <channel_name> <user> <True/False>
  - Allows or denies the specified person from being able to join the specified voice channel. Only the creator of the voice channel can do this!
 
- /remove <channel_name>
  - Removes the voice channel with the specified name. Only the creator of the voice channel and Administrators can do this!
  - Can use partial names to remove channels. E.g: "/remove asdf" works for a channel called "username-asdf-2".
