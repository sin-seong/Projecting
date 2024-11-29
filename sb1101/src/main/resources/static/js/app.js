const puppeteer = require('puppeteer');

// SteamCharts 웹사이트에서 데이터를 크롤링하는 함수
async function fetchSteamChartsData() {
    const browser = await puppeteer.launch({ headless: true });
    const page = await browser.newPage();

    // SteamCharts 페이지로 이동
    await page.goto('https://steamcharts.com/', {
        waitUntil: 'domcontentloaded',  // 페이지가 완전히 로드될 때까지 기다림
    });

    // 게임 데이터를 추출
    const gamesData = await page.evaluate(() => {
        const games = [];
        const gameElements = document.querySelectorAll('.game');

        // 데이터를 제대로 추출하는지 확인
        gameElements.forEach(game => {
            const title = game.querySelector('.gameLink') ? game.querySelector('.gameLink').textContent : 'Unknown';
            const players = game.querySelector('.players') ? game.querySelector('.players').textContent.trim() : 'Unknown';

            if (title !== 'Unknown' && players !== 'Unknown') {
                games.push({
                    title: title,
                    players: players
                });
            }
        });

        return games;
    });

    console.log(gamesData);  // 크롤링된 데이터 출력
    await browser.close();

    return gamesData;


    // 게임 순위와 동접자 수가 담긴 배열 반환
}

// 크롤링된 데이터를 콘솔에 출력
fetchSteamChartsData().then(data => {
    console.log(data); // 예: [{title: "Dota 2", players: "500,000"}, {...}, {...}]
}).catch(console.error);
