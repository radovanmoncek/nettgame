#!/bin/sh

needs_rebuild=0

for name in *.*pp; do
    ([[ ! -e nettgame_websockets ]] || [[ $(stat -c %Y nettgame_websockets) -lt $(stat -c %Y $name) ]]) && echo "$name needs rebuild" && needs_rebuild=1;
done

if [[ $needs_rebuild -eq 1 ]] || ([[ $# -gt 0 ]] && [[ $1 == "recompile" ]]);
then
    kubectl exec nettgame -i -t -- g++ -g -time -o /development/server/nettgame_websockets /development/server/transcenders.cpp /development/framework/internal_service_codec.c
fi

if [[ $(kubectl exec nettgame -- echo $?) -eq 0 ]];
then
    kubectl exec nettgame -- ./development/server/nettgame_websockets 2 /development/client 0.0.0.0 4321 0 10.42.0.86 &
    kubectl exec nettgame2 -i -t -- gdb --args ./development/server/nettgame_websockets 2 /development/client 0.0.0.0 4321 1 10.42.0.94
fi
