const COLS = 10;
const ROWS = 20;
const BLOCK_SIZE = 30;
const COLORS = [
    '#FF0D72', '#0DC2FF', '#0DFF72', '#F538FF', '#FF8E0D', '#FFE138', '#3877FF'
];

// 테트리스 블록 모양
const SHAPES = [
    [[1,1,1,1]],                // I
    [[1,1,1],[0,0,1]],         // J
    [[1,1,1],[1,0,0]],         // L
    [[1,1],[1,1]],             // O
    [[0,1,1],[1,1,0]],         // S
    [[1,1,1],[0,1,0]],         // T
    [[1,1,0],[0,1,1]]          // Z
];
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const nextCanvas = document.getElementById('nextCanvas');
const nextCtx = nextCanvas.getContext('2d');

canvas.width = COLS * BLOCK_SIZE;
canvas.height = ROWS * BLOCK_SIZE;
nextCanvas.width = 4 * BLOCK_SIZE;
nextCanvas.height = 4 * BLOCK_SIZE;

// 게임 변수
let board = Array(ROWS).fill().map(() => Array(COLS).fill(0));
let score = 0;
let gameOver = false;
let currentPiece = null;
let nextPiece = null;
let dropCounter = 0;
let dropInterval = 1000;
let lastTime = 0;
let gameStarted = false;

// 조각 클래스
class Piece {
    constructor(shape, color) {
        this.shape = shape;
        this.color = color;
        this.x = Math.floor(COLS / 2) - Math.floor(shape[0].length / 2);
        this.y = 0;
    }
}

// 새로운 조각 생성
function createPiece() {
    const pieceIndex = Math.floor(Math.random() * SHAPES.length);
    return new Piece(SHAPES[pieceIndex], COLORS[pieceIndex]);
}

// 게임 초기화
function initGame() {
    board = Array(ROWS).fill().map(() => Array(COLS).fill(0));
    score = 0;
    gameOver = false;
    currentPiece = createPiece();
    nextPiece = createPiece();
    document.getElementById('score').textContent = score;
}

// 충돌 검사
function collision(piece, offsetX, offsetY) {
    for (let y = 0; y < piece.shape.length; y++) {
        for (let x = 0; x < piece.shape[y].length; x++) {
            if (piece.shape[y][x]) {
                const newX = piece.x + x + offsetX;
                const newY = piece.y + y + offsetY;
                if (newX < 0 || newX >= COLS || newY >= ROWS) return true;
                if (newY >= 0 && board[newY][newX]) return true;
            }
        }
    }
    return false;
}

// 조각 회전
function rotate(piece) {
    const newShape = piece.shape[0].map((_, i) =>
        piece.shape.map(row => row[i]).reverse()
    );
    const newPiece = new Piece(newShape, piece.color);
    newPiece.x = piece.x;
    newPiece.y = piece.y;
    return newPiece;
}

// 완성된 줄 제거
function clearLines() {
    let linesCleared = 0;
    for (let y = ROWS - 1; y >= 0; y--) {
        if (board[y].every(value => value !== 0)) {
            board.splice(y, 1);
            board.unshift(Array(COLS).fill(0));
            linesCleared++;
            y++;
        }
    }
    if (linesCleared > 0) {
        score += linesCleared * 10;
        document.getElementById('score').textContent = score;
    }
}

// 조각 그리기
function drawPiece(ctx, piece, offsetX = 0, offsetY = 0) {
    piece.shape.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                ctx.fillStyle = piece.color;
                ctx.fillRect(
                    (piece.x + x + offsetX) * BLOCK_SIZE,
                    (piece.y + y + offsetY) * BLOCK_SIZE,
                    BLOCK_SIZE,
                    BLOCK_SIZE
                );
            }
        });
    });
}

// 다음 조각 표시
function drawNextPiece() {
    // 다음 조각 캔버스 초기화
    nextCtx.fillStyle = '#000';
    nextCtx.fillRect(0, 0, nextCanvas.width, nextCanvas.height);

    // 다음 조각의 중앙 위치 계산
    const offsetX = (nextCanvas.width - nextPiece.shape[0].length * BLOCK_SIZE) / 2;
    const offsetY = (nextCanvas.height - nextPiece.shape.length * BLOCK_SIZE) / 2;

    // 다음 조각 그리기
    nextPiece.shape.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                nextCtx.fillStyle = nextPiece.color;
                nextCtx.fillRect(
                    offsetX + x * BLOCK_SIZE,
                    offsetY + y * BLOCK_SIZE,
                    BLOCK_SIZE - 1,
                    BLOCK_SIZE - 1
                );
            }
        });
    });
}

// 게임 보드 그리기
function draw() {
    // 게임 보드 초기화
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 고정된 블록 그리기
    board.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                ctx.fillStyle = value;
                ctx.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
            }
        });
    });

    // 현재 조각 그리기
    currentPiece.shape.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                ctx.fillStyle = currentPiece.color;
                ctx.fillRect(
                    (currentPiece.x + x) * BLOCK_SIZE,
                    (currentPiece.y + y) * BLOCK_SIZE,
                    BLOCK_SIZE - 1,
                    BLOCK_SIZE - 1
                );
            }
        });
    });

    // 다음 조각 그리기
    drawNextPiece();
}

// 조각 고정
function merge() {
    currentPiece.shape.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                board[currentPiece.y + y][currentPiece.x + x] = currentPiece.color;
            }
        });
    });
}

// 게임 오버 처리
function handleGameOver() {
    gameOver = true;
    gameStarted = false;
    document.getElementById('finalScore').textContent = score;
    document.getElementById('gameOverModal').style.display = 'block';
    saveScore(); // 점수 저장 함수 호출
}

// 게임 업데이트
function update(time = 0) {
    if (!gameStarted || gameOver) return;

    const deltaTime = time - lastTime;
    lastTime = time;
    dropCounter += deltaTime;

    if (dropCounter > dropInterval) {
        if (!collision(currentPiece, 0, 1)) {
            currentPiece.y++;
        } else {
            merge();
            clearLines();
            if (currentPiece.y === 0) {
                handleGameOver();
                return;
            }
            currentPiece = nextPiece;
            nextPiece = createPiece();
        }
        dropCounter = 0;
    }

    draw();
    requestAnimationFrame(update);
}

// 키보드 이벤트
document.addEventListener('keydown', event => {
    if (!gameStarted || gameOver) return;

    // 방향키 이벤트에서 기본 동작 막기
    if ([32, 37, 38, 39, 40].includes(event.keyCode)) {  // 32 추가 (스페이스바)
        event.preventDefault(); // 스크롤 방지
    }

    switch (event.keyCode) {
        case 37: // 왼쪽
            if (!collision(currentPiece, -1, 0)) currentPiece.x--;
            break;
        case 39: // 오른쪽
            if (!collision(currentPiece, 1, 0)) currentPiece.x++;
            break;
        case 40: // 아래
            if (!collision(currentPiece, 0, 1)) currentPiece.y++;
            break;
        case 38: // 위 (회전)
            const rotated = rotate(currentPiece);
            if (!collision(rotated, 0, 0)) currentPiece = rotated;
            break;
        case 32: // 스페이스바 (즉시 하강)
            hardDrop();
            break;
    }
});

// 즉시 하강 함수 추가
function hardDrop() {
    while (!collision(currentPiece, 0, 1)) {
        currentPiece.y++;
    }
    merge();
    clearLines();
    if (currentPiece.y === 0) {
        handleGameOver();
        return;
    }
    currentPiece = nextPiece;
    nextPiece = createPiece();
    dropCounter = 0;
}

// 추가: 스페이스바 스크롤 방지
document.addEventListener('keydown', function(e) {
    if (e.code === 'Space' && gameStarted) {
        e.preventDefault();
    }
});
// 게임 시작 버튼
document.getElementById('startButton').addEventListener('click', () => {
    gameStarted = true;
    initGame();
    requestAnimationFrame(update);
});

// 순위 보기 버튼
document.getElementById('rankButton').addEventListener('click', () => {
    window.location.href = '/tetris/ranking';
});

// 점수 등록 버튼
async function saveScore() {
    try {
        const response = await fetch('/tetris/save-score', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ score: score })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        const result = await response.text();
        alert(result);
        window.location.href = '/tetris/ranking';
    } catch (error) {
        console.error('Error:', error);
        if (error.message.includes('로그인이 필요합니다')) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login';
        } else {
            alert('점수 저장에 실패했습니다: ' + error.message);
        }
    }
}

// 게임 오버 모달의 점수 등록 버튼에 이벤트 리스너 추가
document.getElementById('registerScoreButton').addEventListener('click', saveScore);