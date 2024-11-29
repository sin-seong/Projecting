// 미로 게임 초기화
const mazeSize = 15; // 미로 크기 (15x15)
let maze = [];
let playerPosition = { x: 0, y: 0 }; // 플레이어의 초기 위치
let endPosition = { x: mazeSize - 1, y: mazeSize - 1 }; // 끝 위치

// 미로 생성 함수
function generateMaze() {
    for (let y = 0; y < mazeSize; y++) {
        let row = [];
        for (let x = 0; x < mazeSize; x++) {
            row.push(Math.random() > 0.3 ? 0 : 1); // 0은 빈 칸, 1은 벽
        }
        maze.push(row);
    }
    maze[0][0] = 0; // 시작점은 항상 빈 칸
    maze[mazeSize - 1][mazeSize - 1] = 0; // 끝점도 항상 빈 칸
}

// 미로 렌더링 함수
function renderMaze() {
    const mazeElement = document.getElementById('maze');
    mazeElement.innerHTML = ''; // 기존 미로를 지움

    for (let y = 0; y < mazeSize; y++) {
        for (let x = 0; x < mazeSize; x++) {
            const cell = document.createElement('div');
            cell.classList.add('cell');
            if (maze[y][x] === 1) {
                cell.classList.add('wall'); // 벽은 검은색
            }
            if (x === playerPosition.x && y === playerPosition.y) {
                cell.classList.add('player'); // 플레이어 위치는 노란색
            }
            if (x === endPosition.x && y === endPosition.y) {
                cell.classList.add('end'); // 끝 위치는 빨간색
            }
            mazeElement.appendChild(cell);
        }
    }
}

// 플레이어 이동 함수
function movePlayer(direction) {
    const { x, y } = playerPosition;

    if (direction === 'up' && y > 0 && maze[y - 1][x] !== 1) {
        playerPosition.y -= 1;
    } else if (direction === 'down' && y < mazeSize - 1 && maze[y + 1][x] !== 1) {
        playerPosition.y += 1;
    } else if (direction === 'left' && x > 0 && maze[y][x - 1] !== 1) {
        playerPosition.x -= 1;
    } else if (direction === 'right' && x < mazeSize - 1 && maze[y][x + 1] !== 1) {
        playerPosition.x += 1;
    }

    // 미로를 다시 렌더링
    renderMaze();

    // 탈출 성공 여부 확인
    if (playerPosition.x === endPosition.x && playerPosition.y === endPosition.y) {
        alert('축하합니다! 미로를 탈출했습니다!');
    }
}

// 게임 초기화
generateMaze();
renderMaze();
