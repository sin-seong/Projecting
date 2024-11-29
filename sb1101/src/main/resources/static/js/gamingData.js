const express = require('express');
const puppeteer = require('puppeteer');
const app = express();
const PORT = 3000;

// SteamCharts에서 실시간 게임 데이터를 크롤링하는 함수
async function fetchSteamChartsData() {
    const browser = await puppeteer.launch({ headless: true });
    const page = await browser.newPage();
    await page.goto('https://steamcharts.com/');

    const gamesData = await page.evaluate(() => {
        const games = [];
        const gameElements = document.querySelectorAll('.game');

        gameElements.forEach(game => {
            const title = game.querySelector('.gameLink') ? game.querySelector('.gameLink').textContent : 'Unknown';
            const players = game.querySelector('.players') ? game.querySelector('.players').textContent.trim() : 'Unknown';
            games.push({
                title: title,
                players: players
            });
        });

        return games;
    });

    await browser.close();
    return gamesData;
}

// '/getSteamCharts' API를 통해 게임 순위와 동접자 수 데이터를 클라이언트로 전달
app.get('/getSteamCharts', async (req, res) => {
    try {
        const data = await fetchSteamChartsData();

        if (!data || data.length === 0) {
            res.status(404).json({ error: 'No data found' });  // 데이터가 없으면 404 상태 코드 반환
        } else {
            res.json(data);  // 데이터가 있으면 JSON 형식으로 응답
        }
    } catch (error) {
        console.error('Error fetching data:', error);
        res.status(500).json({ error: 'Error fetching data' });
    }
});


// 서버 시작
app.listen(3000, () => {
    console.log('서버가 http://localhost:3000 에서 실행 중입니다.');
});
