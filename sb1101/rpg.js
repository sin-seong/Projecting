function gameUpdate() {
    if (!isGamePaused) {
        player.update();

        monsters.forEach(monster => monster.update(player));
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        player.draw(ctx);
        monsters.forEach(monster => monster.draw(ctx));
        effects.forEach(effect => effect.draw(ctx));
        updateUI();
    }

    gameLoop = requestAnimationFrame(gameUpdate);
}

// 키보드 이벤트 수정
document.addEventListener('keydown', (e) => {
    // 게임 조작키 기본 동작 방지
    if(['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Space'].includes(e.key)) {
        e.preventDefault();
        e.stopPropagation();  // 이벤트 전파도 중지
    }
    
    keys[e.key] = true;
    if (e.code === 'Space' && player) {
        player.attack();
    }
    if (e.code === 'KeyA' && player) {
        player.toggleAutoAttack();
    }
}, { passive: false });  // passive 옵션을 false로 설정

// 게임 캔버스에 대한 추가 이벤트 방지
canvas.addEventListener('keydown', (e) => {
    if(['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Space'].includes(e.key)) {
        e.preventDefault();
        e.stopPropagation();
    }
}, { passive: false });

// 전체 문서에 대한 스크롤 방지
document.addEventListener('scroll', (e) => {
    if (isGamePaused === false) {  // 게임 진행 중일 때만
        e.preventDefault();
        e.stopPropagation();
    }
}, { passive: false }); 

// showAutoAttackStatus 함수를 updateAutoAttackIcon으로 변경
function updateAutoAttackIcon(enabled) {
    const icon = document.getElementById('autoAttackIcon');
    if (enabled) {
        icon.classList.add('active');
    } else {
        icon.classList.remove('active');
    }
}

// Player 클래스의 toggleAutoAttack 메서드 수정
toggleAutoAttack() {
    this.autoAttackEnabled = !this.autoAttackEnabled;
    if (this.autoAttackEnabled) {
        this.startAutoAttack();
    } else {
        this.stopAutoAttack();
    }
    updateAutoAttackIcon(this.autoAttackEnabled);  // 상태 메시지 대신 아이콘 업데이트
}

// 게임 시작시 아이콘 초기화
function startGame() {
    document.body.classList.add('game-active');
    player = new Player(canvas.width/2, canvas.height/2);
    updateAutoAttackIcon(false);  // 아이콘 초기 상태 설정
    // ... 나머지 startGame 코드
}

// 자동 공격 상태 표시 함수 수정
function showAutoAttackStatus(enabled) {
    const statusText = document.createElement('div');
    statusText.style.cssText = `
        position: absolute;
        top: 250px;  // 게임 화면 중앙 근처로 위치 조정
        left: 50%;
        transform: translateX(-50%);
        color: ${enabled ? '#00ff00' : '#ff0000'};
        font-size: 24px;
        font-weight: bold;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.7);
        animation: fadeOut 1.5s forwards;
        z-index: 1000;
        background-color: rgba(0, 0, 0, 0.7);
        padding: 10px 20px;
        border-radius: 10px;
        pointer-events: none;
    `;
    statusText.textContent = enabled ? "자동 공격 활성화" : "자동 공격 비활성화";
    document.body.appendChild(statusText);
    
    // 애니메이션 스타일 추가
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeOut {
            0% { opacity: 1; transform: translate(-50%, 0); }
            50% { opacity: 1; transform: translate(-50%, 0); }
            100% { opacity: 0; transform: translate(-50%, -30px); }
        }
    `;
    document.head.appendChild(style);
    
    setTimeout(() => {
        statusText.remove();
        style.remove();
    }, 1500);
} 