// 캔버스 설정
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
canvas.width = 800;
canvas.height = 600;

// 전역 변수
let player = null;
let monsters = [];
let effects = [];
let gameLoop;
let spawnInterval = 2000; // 초기 스폰 간격 (2초)
let minSpawnInterval = 50; // 최소 스폰 간격 (0.005초)
let spawnTimer;
let autoAttackTimer;
let keys = {};
let isGamePaused = false;
let killCount = 0;
const BOSS_SPAWN_KILL_COUNT = 30; // 30킬마다 보스 등장
let bossKillCount = 0; // 보스 처치 수 카운트
let monsterStatMultiplier = 1.0; // 몬스터 스탯 배율

// SPRITE_CONFIG를 클래스 외부로 이동
const SPRITE_CONFIG = {
    sheetWidth: 254,     // 전체 스프라이트 시트 너비
    sheetHeight: 224,    // 전체 스프라이트 시트 높이
    framesPerRow: 8,     // 한 줄당 프레임 수
    rows: 5,             // 애니메이션 있는 행 수
    frameWidth: 254 / 8, // 한 프레임의 너비 (약 31.75px)
    frameHeight: 224 / 7,// 한 프레임의 높이 (약 32px)
    types: {
        ORANGE: {row:0, chance :70, multiplier: 1},  // 주황색 불꽃
        BLUE: { row: 1, chance: 15, multiplier: 1.5 },    // 강한 몬스터 (15% 확률)
        PINK: { row: 2, chance: 10, multiplier: 0.8 },    // 힐링 몬스터 (10% 확률)
        BLACK: { row: 3, chance: 5, multiplier: 1.2 }     // 경험치 몬스터 (5% 확률)
    }

};

// Player 클래스
class Player {
    constructor(x, y) {

        this.sprite = new Image();0
        this.sprite.src = '/images/character3.png'

        this.x = x;
        this.y = y;
        this.width = 32;
        this.height = 36;
        this.speed = 5;
        this.hp = 100;
        this.maxHp = 100;
        this.level = 1;
        this.exp = 0;
        this.maxExp = 100;
        this.attackDamage = 20;
        this.attackRange = 60;
        this.isAttacking = false;

        // 자동 공격 관련 속성 추가
        this.autoAttackEnabled = false;
        this.autoAttackSpeed = 1000; // 1초
        this.autoAttackTimer = null;

        // 스프라이트 프레임 설정
        this.frameX = 0;          // 현재 프레임 (0-2)
        this.frameY = 0;          // 방향 (0: 아래, 1: 왼쪽, 2: 오른쪽, 3: 위)
        this.frameWidth = 16;     // 한 프레임의 너비
        this.frameHeight = 18;    // 한 프레임의 높이
        this.frameCount = 3;      // 각 방향당 프레임 수
        this.animationSpeed = 10; // 애니메이션 속도
        this.animationCounter = 0;  // 이 줄 추가
        this.displayWidth = 32;
        this.displayHeight = 36;

        // 캐릭터 크기 설정
        this.width = 32;          // 실제 게임에서의 캐릭터 크기
        this.height = 32;
    }

    // 자동 공격 토글 메서드
    toggleAutoAttack() {
        this.autoAttackEnabled = !this.autoAttackEnabled;
        if (this.autoAttackEnabled) {
            this.startAutoAttack();
        } else {
            this.stopAutoAttack();
        }
    }

    // 자동 공격 시작
    startAutoAttack() {
        if (this.autoAttackTimer) clearInterval(this.autoAttackTimer);
        this.autoAttackTimer = setInterval(() => {
            if (!isGamePaused) {
                this.attack();
            }
        }, this.autoAttackSpeed);
    }

    // 자동 공격 중지
    stopAutoAttack() {
        if (this.autoAttackTimer) {
            clearInterval(this.autoAttackTimer);
            this.autoAttackTimer = null;
        }
    }

    update() {
        // 키보드 입력에 따른 이동
        let dx = 0;
        let dy = 0;

        if (keys['ArrowLeft']) {
            dx = -1;
            this.frameY = 3;
        }
        if (keys['ArrowRight']) {
            dx = 1;
            this.frameY = 1;
        }
        if (keys['ArrowUp']) {
            dy = -1;
            this.frameY = 0;
        }
        if (keys['ArrowDown']) {
            dy = 1;
            this.frameY = 2;
        }

        // 대각선 이동 시 속도 보정
        if (dx !== 0 && dy !== 0) {
            dx *= 0.707;
            dy *= 0.707;
        }

        // 위치 업데이트
        this.x += dx * this.speed;
        this.y += dy * this.speed;

        // 화면 경계 체크
        this.x = Math.max(0, Math.min(canvas.width - this.width, this.x));
        this.y = Math.max(0, Math.min(canvas.height - this.height, this.y));
        if (this.hp <= 0) {
            gameOver();
            return;
        }
        this.animationCounter++;
        if (this.animationCounter >= this.animationSpeed) {
            this.animationCounter = 0;
            this.frameX = (this.frameX + 1) % this.frameCount;
        }

        // 애니메이션 업데이트
        this.updateAnimation();
    }

    updateAnimation() {
        if (keys['ArrowLeft'] || keys['ArrowRight'] ||
            keys['ArrowUp'] || keys['ArrowDown']) {


            // 방향 설정
            if (keys['ArrowDown']) {
                this.frameY = 2;  // 아래
            } else if (keys['ArrowLeft']) {
                this.frameY = 3;  // 왼쪽
            } else if (keys['ArrowRight']) {
                this.frameY = 1;  // 오른쪽
            } else if (keys['ArrowUp']) {
                this.frameY = 0;  // 위
            }
        } else {
            this.frameX = 1;  // 정지 시 기본 포즈 (가운데 프레임)
        }
    }

    drawHealthBar(ctx) {
        const barWidth = this.width;
        const barHeight = 5;
        const barY = this.y - 10;

        // 체력바 배경
        ctx.fillStyle = 'red';
        ctx.fillRect(this.x, barY, barWidth, barHeight);

        // 현재 체력
        ctx.fillStyle = 'green';
        ctx.fillRect(this.x, barY, barWidth * (this.hp / this.maxHp), barHeight);

    }

    attack() {
        this.isAttacking = true;

        monsters.forEach((monster, index) => {
            const dx = monster.x - this.x;
            const dy = monster.y - this.y;
            const distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < this.attackRange) {
                monster.hp -= this.attackDamage;

                if (monster.hp <= 0) {
                    // 경험치 획득
                    this.gainExp(monster.exp);

                    // 킬 카운트 증가
                    killCount++;

                    // 보스 몬스터 처치 시
                    if (monster.isBoss) {
                        bossKillCount++;
                        // 4마리 처치마다 몬스터 체력 20% 증가
                        if (bossKillCount % 4 === 0) {
                            monsterStatMultiplier *= 1.2;
                        }
                        spawnInterval = Math.max(spawnInterval * 0.8, minSpawnInterval);
                        showBossDefeatEffect(monster.x, monster.y);
                    }

                    // 30킬마다 보스 스폰
                    if (killCount % BOSS_SPAWN_KILL_COUNT === 0 && !monster.isBoss) {
                        spawnBossMonster();
                    }

                    monsters.splice(index, 1);
                }
            }
        });

        setTimeout(() => {
            this.isAttacking = false;
        }, 100);
    }

    gainExp(amount) {
        this.exp += amount;
        if (this.exp >= this.maxExp) {
            this.levelUp();
        }
    }

    levelUp() {
        this.level++;
        this.exp = 0;
        this.maxExp *= 1.2;

        // 게임 일시정지
        isGamePaused = true;

        // 레벨업 UI 표시
        showLevelUpChoices();
    }

    draw(ctx) {
        if (this.sprite.complete) {
            ctx.imageSmoothingEnabled =false;
            ctx.globalCompositeOperation = 'destination-out';
            ctx.fillStyle='rgb(255,255,255)';
            ctx.fillRect(this.x, this.y,this.displayWidth, this.displayHeight);

            ctx.globalCompositeOperation='source-over';
            // 현재 프레임과 방향에 따라 스프라이트 시트에서 해당 부분을 그림
            ctx.drawImage(
                this.sprite,
                this.frameX * this.frameWidth,    // 스프라이트 시트 X 위치
                this.frameY * this.frameHeight,   // 스프라이트 시트 Y 위치
                this.frameWidth,                  // 프레임 너비
                this.frameHeight,                 // 프레임 높이
                this.x,                          // 캔버스 X 위치
                this.y,                          // 캔버스 Y 위치
                this.displayWidth,               // 화면에 표시될 너비
                this.displayHeight                      // 그려질 높이
            );

            // 체력바와 공격 범위는 그대로 유지
            this.drawHealthBar(ctx);
            if (this.isAttacking) {
                ctx.strokeStyle = 'yellow';
                ctx.beginPath();
                ctx.arc(this.x + this.width/2, this.y + this.height/2,
                       this.attackRange, 0, Math.PI * 2);
                ctx.stroke();
            }
        }
    }

}

// Monster 클래스
class Monster {

    constructor(x, y, isBoss = false) {

        this.x = x;
        this.y = y;
        this.width = isBoss ? 60 : 30;  // 보스는 2배 크기
        this.height = isBoss ? 60 : 30;
        this.speed = isBoss ? 1 : 2;    // 보스는 느리게 이동

        this.type= this.getRandomMonsterType();

        this.sprite = new Image();
        this.sprite.src='/images/monster-sprites.png';

        // 기본 스탯에 배율 적용
        let baseHp = 50 * monsterStatMultiplier;
        let baseDamage = 10 * monsterStatMultiplier;

        // 스프라이트 프레임 설정
        this.frameX = 0;
        this.frameY = isBoss ?1 : 0;
        this.frameWidth = SPRITE_CONFIG.frameWidth;
        this.frameHeight = SPRITE_CONFIG.frameHeight;
        this.frameCount = SPRITE_CONFIG.framesPerRow;
        this.animationSpeed = 10;  // 오타 수정
        this.animationCounter = 0;  // 초기값 수정

        // 보스는 추가로 2배
        this.hp = isBoss ? baseHp * 2 : baseHp;
        this.maxHp = this.hp;
        this.damage = isBoss ? baseDamage * 2 : baseDamage;
        this.isBoss = isBoss;
        let baseExp = isBoss ? 100 : 20;    // 보스는 5배 경험치
        this.exp = this.type.row === SPRITE_CONFIG.types.BLACK.row ? baseExp * 4 : baseExp;

        this.color = this.getMonsterColor();

        console.log('몬스터생성 완료',this);
    }
    getMonsterType(){
        switch(this.type.row) {
            case SPRITE_CONFIG.types.ORANGE.row: return '주황색(기본)';
            case SPRITE_CONFIG.types.BLUE.row: return '파란색(강함)';
            case SPRITE_CONFIG.types.PINK.row: return '분홍색(힐링)';
            case SPRITE_CONFIG.types.BLACK.row: return '검은색(경험치)';
            default: return '알 수 없음';
        }

    }
    getMonsterColor() {
        switch(this.type.row) {
            case SPRITE_CONFIG.types.ORANGE.row: return 'orange';
            case SPRITE_CONFIG.types.BLUE.row: return 'blue';
            case SPRITE_CONFIG.types.PINK.row: return 'pink';
            case SPRITE_CONFIG.types.BLACK.row: return 'black';
            default: return 'red';
        }
    }

    getRandomMonsterType() {
        const rand = Math.random() * 100;
        let cumulative  = 0;

        for (let type of Object.values(SPRITE_CONFIG)){
            cumulative += type.chance;
            if(rand <= cumulative){
                return type;
            }
        }
        return SPRITE_CONFIG.types.ORANGE;
    }


    drawHealthBar(ctx) {
        const barWidth = this.width;
        const barHeight = 5;
        const barY = this.y - 10;

        // 체력바 배경
        ctx.fillStyle = 'red';
        ctx.fillRect(this.x, barY, barWidth, barHeight);

        // 현재 체력
        ctx.fillStyle = 'green';
        ctx.fillRect(this.x, barY, barWidth * (this.hp / this.maxHp), barHeight);

        ctx.fillStyle = this.color;
        ctx.fillRect(this.x, barY, barWidth * (this.hp/this.maxHp),barHeight);

        ctx.fillStyle='white';
        ctx.font = '10px Arial';
        ctx.fillText(this.getMonsterType(),this.x, barY -5);
    }
    onCollisionWithPlayer(player){
        player.hp -= this.damage * (1/60);

        if(this.type.row === SPRITE_CONFIG.types.PINK){
            player.hp= Math.min(player.hp+0.5,player.maxHp);
        }
    }

    update(player) {
        const dx = player.x - this.x;
        const dy = player.y - this.y;
        const distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            this.x += (dx / distance) * this.speed;
            this.y += (dy / distance) * this.speed;
        }

        if (this.checkCollision(player)) {
            this.onCollisionWithPlayer(player);
        }

        // 애니메이션 업데이트
        this.animationCounter++;
        if (this.animationCounter >= this.animationSpeed) {
            this.animationCounter = 0;
            this.frameX = (this.frameX + 1) % this.frameCount;
        }
    }

    checkCollision(player) {
        return this.x < player.x + player.width &&
               this.x + this.width > player.x &&
               this.y < player.y + player.height &&
               this.y + this.height > player.y;
    }

    draw(ctx) {
        if (this.sprite.complete) {
            ctx.drawImage(
                this.sprite,
                this.frameX * this.frameWidth,
                this.frameY * this.frameHeight,
                this.frameWidth,
                this.frameHeight,
                this.x,
                this.y,
                this.width,
                this.height
            );
            this.drawHealthBar(ctx);
            }
    }
}

// 게임 시작
function startGame() {
    document.body.classList.add('game-active');
    // player = new Player(canvas.width/2, canvas.height/2);
    // updateAutoAttackIcon(false);

    player = new Player(canvas.width/2, canvas.height/2);
    monsters = [];
    effects = [];
    killCount = 0;
    bossKillCount = 0;
    monsterStatMultiplier = 1.0;
    isGamePaused = false;
    spawnInterval = 2000; // 초기 스폰 간격 리셋

    spawnTimer = setInterval(() => {
        if (!isGamePaused) {
            spawnMonster();
        }
    }, spawnInterval);

    if (gameLoop) cancelAnimationFrame(gameLoop);
    gameLoop = requestAnimationFrame(gameUpdate);

    if (player && player.autoAttackEnabled) {
        player.stopAutoAttack();
    }
}

// 게임 업데이트
function gameUpdate() {
    if (!isGamePaused) {

        player.update();  // 이렇게 수정

        // 나머지 코드...
        monsters.forEach(monster => monster.update(player));
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        player.draw(ctx);
        monsters.forEach(monster => monster.draw(ctx));
        effects.forEach(effect => effect.draw(ctx));
        updateUI();
    }
    if(!isGamePaused){
        if(player.hp<=0){
            gameOver();
            return;
        }
    }

    gameLoop = requestAnimationFrame(gameUpdate);
}

function gameOver() {
    isGamePaused = true;
    cancelAnimationFrame(gameLoop);
    if (spawnTimer) clearInterval(spawnTimer);
    if (player.autoAttackTimer) clearInterval(player.autoAttackTimer);

    const gameOverUI = document.createElement("div");
    gameOverUI.id = 'gameOverUI';  // ID 추가
    gameOverUI.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background-color: rgba(0, 0, 0, 0.9);
        padding: 20px;
        border-radius: 10px;
        color: white;
        text-align: center;
        z-index: 1000;
    `;

    gameOverUI.innerHTML = `
        <h2>게임 오버!</h2>
        <p>최종 점수: ${killCount}킬</p>
        <p>보스 처치: ${bossKillCount}</p>
        <p>레벨: ${player.level}</p>
        <button onclick="restartGame()" style="
            padding: 10px 20px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            margin-top: 10px;
        ">다시 시작</button>
    `;

    document.body.appendChild(gameOverUI);
}

function restartGame() {
    const gameOverUI = document.querySelector('#gameOverUI');
    if (gameOverUI) {
        gameOverUI.remove();
    }
    startGame();
}
// 몬스터 스폰
function spawnMonster() {
    // if (isGamePaused) return;

    const side = Math.floor(Math.random() * 4);
    let x, y;

    switch(side) {
        case 0: // 위
            x = Math.random() * canvas.width;
            y = -30;
            break;
        case 1: // 오른쪽
            x = canvas.width + 30;
            y = Math.random() * canvas.height;
            break;
        case 2: // 아래
            x = Math.random() * canvas.width;
            y = canvas.height + 30;
            break;
        case 3: // 왼쪽
            x = -30;
            y = Math.random() * canvas.height;
            break;
    }

    try {
        const monster = new Monster(x, y);
        monsters.push(monster);
        console.log('몬스터 생성됨:', x, y); // 디버깅용 로그
    } catch (error) {
        console.error('몬스터 생성 중 오류:', error); // 에러 로깅
    }
}

// UI 업데이트
function updateUI() {
    // 기본 스탯
    const hpElement = document.getElementById('hp');
    const maxHpElement = document.getElementById('maxHp');
    const levelElement = document.getElementById('level');
    const expElement = document.getElementById('exp');
    const maxExpElement = document.getElementById('maxExp');
    const attackElement = document.getElementById('attack');
    const attackSpeedElement = document.getElementById('attackSpeed');
    const speedElement = document.getElementById('speed');
    const rangeElement = document.getElementById('range');
    const killCountElement = document.getElementById('killCount');
    const bossKillCountElement = document.getElementById('bossKillCount');
    const monsterPowerElement = document.getElementById('monsterPower');

    // UI 업데이트

        if (hpElement) hpElement.textContent = Math.floor(player.hp);
        if (maxHpElement) maxHpElement.textContent = player.maxHp;
        if (levelElement) levelElement.textContent = player.level;
        if (expElement) expElement.textContent = Math.floor(player.exp);
        if (maxExpElement) maxExpElement.textContent = player.maxExp;
        if (attackElement) attackElement.textContent = Math.floor(player.attackDamage);
        if (attackSpeedElement) attackSpeedElement.textContent = (player.autoAttackSpeed / 1000).toFixed(1);
        if (speedElement) speedElement.textContent = player.speed;
        if (rangeElement) rangeElement.textContent = player.attackRange;
        if (killCountElement) killCountElement.textContent = killCount;
        if (bossKillCountElement) bossKillCountElement.textContent = bossKillCount;
        if (monsterPowerElement) monsterPowerElement.textContent = monsterStatMultiplier.toFixed(1);


    // 추가 스탯 정보 표시
    const statsElement = document.getElementById('playerStats');
    if (statsElement) {
        statsElement.innerHTML = `
            공격력: ${Math.floor(player.attackDamage)} |
            이동속도: ${player.speed} |
            공격범위: ${player.attackRange} |
            보스 처치: ${bossKillCount} |
            몬스터 배율: ${monsterStatMultiplier.toFixed(1)}x
        `;
    }
}

// 이벤트 리스너
document.addEventListener('DOMContentLoaded', () => {
    // 키보드 이벤트
    document.addEventListener('keydown', (e) => {
        if(['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Space'].includes(e.key)) {
            e.preventDefault();
            e.stopPropagation();  // 이벤트 전파도 중지
        }

        keys[e.key] = true;
        if (e.code === 'Space' && player) {
            player.attack();
        }
        // A 키로 자동 공격 토글
        if (e.code === 'KeyA' && player) {
            player.toggleAutoAttack();
            showAutoAttackStatus(player.autoAttackEnabled);
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
    }, { passive: false }
    );

    document.addEventListener('keyup', (e) => {
        keys[e.key] = false;
    });

    // 시작 버튼
    const startButton = document.getElementById('startButton');
    if (startButton) {
        startButton.addEventListener('click', startGame);
    }
});

// 레벨업 UI 관련 함수들 추가
function showLevelUpChoices() {
    const levelUpUI = document.createElement('div');
    levelUpUI.id = 'levelUpUI';
    levelUpUI.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background-color: rgba(0, 0, 0, 0.9);
        padding: 20px;
        border-radius: 10px;
        color: white;
        text-align: center;
        z-index: 1000;
    `;

    levelUpUI.innerHTML = `
        <h2>레벨 업!</h2>
        <p>능력치를 선택하세요:</p>
        <button onclick="selectUpgrade('hp')" class="upgrade-btn">최대 체력 증가 (+20)</button>
        <button onclick="selectUpgrade('damage')" class="upgrade-btn">공격력 증가 (+5)</button>
        <button onclick="selectUpgrade('speed')" class="upgrade-btn">이동속도 증가 (+1)</button>
        <button onclick="selectUpgrade('range')" class="upgrade-btn">공격범위 증가 (+10)</button>
        <button onclick="selectUpgrade('attackSpeed')" class="upgrade-btn">자동공격 속도 증가 (20% 감소)</button>
    `;

    // 버튼 스타일 추가
    const style = document.createElement('style');
    style.textContent = `
        .upgrade-btn {
            display: block;
            width: 200px;
            margin: 10px auto;
            padding: 10px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        .upgrade-btn:hover {
            background-color: #45a049;
        }
    `;
    document.head.appendChild(style);

    document.body.appendChild(levelUpUI);
}

function selectUpgrade(type) {
    switch(type) {
        case 'hp':
            player.maxHp *= 1.2;
            // player.hp = player.maxHp;
            break;
        case 'damage':
            player.attackDamage += 5;
            break;
        case 'speed':
            player.speed += 1;
            break;
        case 'range':
            player.attackRange += 10;
            break;
        case 'attackSpeed':
            player.autoAttackSpeed *= 0.8; // 20% 감소
            if (player.autoAttackEnabled) {
                player.stopAutoAttack();
                player.startAutoAttack();
            }
            break;
    }

    // 레벨업 UI 제거
    const levelUpUI = document.getElementById('levelUpUI');
    if (levelUpUI) {
        levelUpUI.remove();
    }

    // 게임 재개
    isGamePaused = false;

    // 레벨업 효과 표시
    showLevelUpEffect();
}

function showLevelUpEffect() {
    // 레벨업 효과 애니메이션
    const effect = {
        x: player.x,
        y: player.y,
        duration: 60,
        draw: function(ctx) {
            if (this.duration > 0) {
                ctx.strokeStyle = `rgba(255, 215, 0, ${this.duration/60})`;
                ctx.lineWidth = 2;
                ctx.beginPath();
                ctx.arc(
                    this.x + player.width/2,
                    this.y + player.height/2,
                    40 - (this.duration/60) * 20,
                    0,
                    Math.PI * 2
                );
                ctx.stroke();
                this.duration--;
            }
        }
    };
    effects.push(effect);
}

// 보스 몬스터 스폰 함수
function spawnBossMonster() {
    const side = Math.floor(Math.random() * 4);
    let x, y;

    switch(side) {
        case 0: // 위
            x = Math.random() * canvas.width;
            y = -60;
            break;
        case 1: // 오른쪽
            x = canvas.width + 60;
            y = Math.random() * canvas.height;
            break;
        case 2: // 아래
            x = Math.random() * canvas.width;
            y = canvas.height + 60;
            break;
        case 3: // 왼쪽
            x = -60;
            y = Math.random() * canvas.height;
            break;
    }

    monsters.push(new Monster(x, y, true));

    // 보스 등장 알림
    showBossWarning();
}

// 보스 등장 경고 효과
function showBossWarning() {
    const warning = document.createElement('div');
    warning.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        color: red;
        font-size: 24px;
        font-weight: bold;
        text-shadow: 2px 2px 4px black;
        animation: fadeOut 2s forwards;
    `;
    warning.textContent = "보스 몬스터 출현!";
    document.body.appendChild(warning);

    setTimeout(() => warning.remove(), 2000);
}

// 보스 처치 효과
function showBossDefeatEffect(x, y) {
    const effect = {
        x: x,
        y: y,
        duration: 60,
        draw: function(ctx) {
            if (this.duration > 0) {
                ctx.strokeStyle = `rgba(128, 0, 0, ${this.duration/60})`;
                ctx.lineWidth = 3;
                ctx.beginPath();
                ctx.arc(
                    this.x,
                    this.y,
                    60 - (this.duration/60) * 30,
                    0,
                    Math.PI * 2
                );
                ctx.stroke();
                this.duration--;
            }
        }
    };
    effects.push(effect);
}

// 자동 공격 상태 표시 함수
function showAutoAttackStatus(enabled) {
    const statusText = document.createElement('div');
    statusText.style.cssText = `
        position: absolute;
        top: 250px;
        left: 50%;
        transform: translateX(-50%);
        color: ${enabled ? 'green' : 'red'};
        font-size: 20px;
        font-weight: bold;
        text-shadow: 2px 2px 4px rgba(0,0,0,0.5);
        animation: fadeOut 2s forwards;
        z-index:1000;
        ackground-color: rgba(0, 0, 0, 0.5);
        padding: 5px 15px;
        border-radius: 5px;
    `;
    statusText.textContent = enabled ? "자동 공격 활성화" : "자동 공격 비활성화";
    document.body.appendChild(statusText);

    setTimeout(() => statusText.remove(), 1000);
}

// 캐릭터의 기본 설정
class Character {
    constructor() {
        this.x = 100;
        this.y = 100;
        this.speed = 3;

        // 스프라이트 크기 수정
        this.frameWidth = 64;    // 32에서 64로 수정
        this.frameHeight = 64;   // 32에서 64로 수정
        this.frameX = 0;
        this.frameY = 0;
        this.frameCount = 9;

        // 애니메이션 관련
        this.animationCounter = 0;
        this.animationSpeed = 5;  // 값이 작을수록 애니메이션이 빠름
    }

    // update() {
    //     // 이전 위치 저장
    //     const prevX = this.x;
    //     const prevY = this.y;
    //
    //     // 키보드 입력에 따른 캐릭터 이동
    //     if (keys['ArrowLeft']) {
    //         this.x -= this.speed;
    //         this.frameY = 3;
    //     }
    //     if (keys['ArrowRight']) {
    //         this.x += this.speed;
    //         this.frameY = 1;
    //     }
    //     if (keys['ArrowUp']) {
    //         this.y -= this.speed;
    //         this.frameY = 0;
    //     }
    //     if (keys['ArrowDown']) {
    //         this.y += this.speed;
    //         this.frameY = 2;
    //     }
    //
    //     // 화면 경계 체크 (선택사항)
    //     if (this.x < 0) this.x = 0;
    //     if (this.x > canvas.width - this.frameWidth) this.x = canvas.width - this.frameWidth;
    //     if (this.y < 0) this.y = 0;
    //     if (this.y > canvas.height - this.frameHeight) this.y = canvas.height - this.frameHeight;
    //
    //     this.animationCounter++;
    //     if(this.animationCounter >= this.animationSpeed){
    //         this.animationCounter = 0;
    //         this.frameX = (this.frameX + 1 ) % this.frameCount;
    //
    //     }
    //
    //     this.updateAnimation();
    // }
    //
    // updateAnimation() {
    //     if (keys['ArrowLeft'] || keys['ArrowRight'] ||
    //         keys['ArrowUp'] || keys['ArrowDown']) {
    //

    //     } else {
    //         this.frameX = 1;  // 정지 시 기본 포즈
    //     }
    //
    //
    // }

    draw(ctx) {
        const image = document.getElementById('characterImage');
        ctx.drawImage(
            image,
            this.frameX * this.frameWidth,
            this.frameY * this.frameHeight,
            this.frameWidth,
            this.frameHeight,
            this.x,
            this.y,
            this.frameWidth,
            this.frameHeight
        );
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

}