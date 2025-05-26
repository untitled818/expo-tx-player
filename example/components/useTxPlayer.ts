import { useMemo } from "react";
import { Player } from "./Player";

export function useTxPlayer(
  source: string,
  setup?: (player: Player) => void
): Player {
  const player = useMemo(() => {
    const p = Player.source(source);
    setup?.(p);
    return p;
  }, [source]);

  return player;
}
