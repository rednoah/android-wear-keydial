latinSquare = require('latin-square')


var pilotStudySize = parseInt(process.argv[2])
var pilotStudyLayouts = process.argv.slice(3, process.argv.length)



for (var i = 1; i <= pilotStudySize; ) {
    var sampler = latinSquare(pilotStudyLayouts)

    for (var j = 0; j < pilotStudyLayouts.length; j++, i++) {
        var a = sampler()
        console.log(i + '\t' + a.join('\t'))
    }
}
