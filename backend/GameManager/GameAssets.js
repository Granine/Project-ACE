class gameAssets {
    /* return a 0-36 roulette table's location maped to colour
    @return {array} rouletteTable: a 0-36 table with colour
    */
    static getRoulette() {
        return [
            'green',
            'red',
            'black',
            'red',
            'black',
            'red',      // 5
            'black',
            'red',
            'black',
            'red',
            'black',    //10
            'black',
            'red',
            'black',
            'red',
            'black',
            'red',
            'black',
            'red',
            'red',
            'black',
            'red',
            'black',
            'red',
            'black',
            'red',
            'black',
            'red',
            'black',
            'black',
            'red',
            'black',
            'red',
            'black',
            'red',
            'black',
            'red'
        ]   // a 0-36 table  
    }
    /* return a set of Pokar cards
    @param {boolean} addJockers: if true, add jockers to the set
    @return {array} pokar: a set of pokar cards
    */
    static getPokar(addJockers = false) {
        let pokarSet = []
        if (addJockers) {
            pokarSet = [    // 54 cards
                '2H', '2D', '2C', '2S',
                '3H', '3D', '3C', '3S',
                '4H', '4D', '4C', '4S',
                '5H', '5D', '5C', '5S',
                '6H', '6D', '6C', '6S',
                '7H', '7D', '7C', '7S',
                '8H', '8D', '8C', '8S',
                '9H', '9D', '9C', '9S',
                '10H', '10D', '10C', '10S',
                'JH', 'JD', 'JC', 'JS',
                'QH', 'QD', 'QC', 'QS',
                'KH', 'KD', 'KC', 'KS',
                'AH', 'AD', 'AC', 'AS',
                'bigJoker', 'smallJoker'
            ]
        } else {
            pokarSet = [    // 52 cards
                '2H', '2D', '2C', '2S',
                '3H', '3D', '3C', '3S',
                '4H', '4D', '4C', '4S',
                '5H', '5D', '5C', '5S',
                '6H', '6D', '6C', '6S',
                '7H', '7D', '7C', '7S',
                '8H', '8D', '8C', '8S',
                '9H', '9D', '9C', '9S',
                '10H', '10D', '10C', '10S',
                'JH', 'JD', 'JC', 'JS',
                'QH', 'QD', 'QC', 'QS',
                'KH', 'KD', 'KC', 'KS',
                'AH', 'AD', 'AC', 'AS',
            ]
        }
        return pokarSet;
    }

    /* return the numerical value of the card
    a = 1, j = 11, q = 12, k = 13
    small joker = 100, big jokar = 101
    @param {str} card: the card to be calculated, one defined in getPokar()
    @return {int} value: the value of the card
    */
    static getPokarFaceValue(card) {
        let value = 0;
        if (card.includes('Joker')) {
            value = card.includes('big') ? 101 : 100;
        } else {
            switch (card[0]) {
                case 'A':
                    value = 1;
                    break;
                case 'J':
                    value = 11;
                    break;
                case 'Q':
                    value = 12;
                    break;
                case 'K':
                    value = 13;
                    break;
                default:
                    value = parseInt(card);
            }
        }
        return value;
    }
}

module.exports = gameAssets;