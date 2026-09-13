#!/bin/sh

needs_rebuild=0

for name in *.*pp; do
    ([[ ! -e nettgame_websockets ]] || [[ $(stat -c %Y nettgame_websockets) -lt $(stat -c %Y $name) ]]) && echo "$name needs rebuild" && needs_rebuild=1;
done

if [[ $needs_rebuild -eq 1 ]] || ([[ $# -gt 0 ]] && [[ $1 == "recompile" ]]);
then
    kubectl exec nettgame -i -t -- g++ -g -time -o /development/server/nettgame_websockets /development/server/nettgame_game.cpp;
fi

if [[ $(kubectl exec nettgame -- echo $?) -eq 0 ]];
then
    kubectl exec nettgame -- ./development/server/nettgame_websockets 0.0.0.0 4321 2 0 /development/client 10.42.0.86 &
    kubectl exec nettgame2 -i -t -- gdb --args ./development/server/nettgame_websockets 0.0.0.0 4321 2 1 /development/client 10.42.0.88
fi
