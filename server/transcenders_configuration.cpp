#define MIN_ARGV 4
#define MIN_ASIO_THREADS 1
/**
 * Synopsis:
 *
 * 1/60.f is approx. 16ms (960)
 */
#define GAME_SESSION_TICK_RATE std::chrono::milliseconds(16)
#define MAX_PLAYER_NAME_LENGTH 8
#define MAX_JUMP_HEIGHT 2
#define MAX_TRANSFER_BUFFER_SIZE 32
#define MAX_PLAYERS 2
#define MAX_THREATHS 10
#define WORLD_WIDTH 40
#define WORLD_HEIGHT 8
#define THREAT_IDLE_TICKS 2
#define THREAT_MOVEMENT_DURATION 32
#define HITBOX_SIZE 8
#define THREAT_SPEED 0.05
#define HITSCAN_SHOT_SPEED 0.1
#define MAX_SPAWNED_ITEMS_WEAPONS 1
