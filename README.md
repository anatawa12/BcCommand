# BcCommand

adds `/bc` command. helps to make roads.
this mod is a clone of the script: [BezierCurveScript (nicovideo)](https://www.nicovideo.jp/watch/sm31993546)

# how to use

1. select blocks with selection_wand. you can use it like World Edit. 
you have to use command to get it
1. place anchor blocks. The list of blocks is shown below:
    - P1, start of the curve: sponge
    - P2: Diamond block (optional in line mode)
    - P3: Emerald block (optional in line mode)
    - P4, end of the curve: end stone
1. call `/bc` command

you can undo with `///undo` and redo with `///redo`

## `bc` command
`/bc <per blocks> <offset> [keep] [force] [line] [rtm]`

`<per blocks>` \
: **optional** places at specified intervals. defaults 0 (as possible as small interval)

`<offset>`\
: **optional** first interval.

`keep`\
: keeps bezier curve anchor blocks. if not set, anchor blocks are replaced to glass.

`force`\
: place blocks whatever block are placed. if not set, place block if the block is air.

`line`\
: make line. not a bezier curve. you does not have to place P2 and P3 anchor.

`rtm`\
: Keep gradient constant. this is a option for RTM.

# difference between BezierCurveScript and this mod

- air mode is on by default. you can use `force`.
- command name for undo and redo was changed to `///undo` and `///redo`
- 
