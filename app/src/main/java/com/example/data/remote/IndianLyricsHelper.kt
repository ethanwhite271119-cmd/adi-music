package com.example.data.remote

import com.example.data.model.LyricLine

object IndianLyricsHelper {

    /**
     * Checks if a title or artist is an Indian / Bollywood track or artist.
     */
    fun isIndianMusic(title: String, artist: String): Boolean {
        val query = "${title.lowercase()} ${artist.lowercase()}"
        val indianKeywords = listOf(
            "kesariya", "tum hi ho", "apna bana le", "channa mereya", "raataan lambiyan",
            "husn", "pasoori", "tera ban jaunga", "kahani suno", "maan meri jaan",
            "arijit", "pritam", "shreya ghoshal", "anuv jain", "jubin nautiyal",
            "asees kaur", "king", "kaifi khalil", "sachin-jigar", "mithoon",
            "atif aslam", "ar rahman", "sidhu moose wala", "diljit dosanjh",
            "badshah", "raftaar", "darshan raval", "neha kakkar", "sonu nigam",
            "alka yagnik", "kishore kumar", "lata mangeshkar", "bollywood", "hindi",
            "punjabi", "desi", "sufi", "bhajan", "ghazal"
        )
        return indianKeywords.any { query.contains(it) } || query.any { it in '\u0900'..'\u097F' }
    }

    /**
     * Attempts to retrieve curated, time-synced Hindi & English bilingual lyrics for known Indian hits.
     */
    fun getCuratedIndianLyrics(title: String, artist: String): List<LyricLine>? {
        val query = "${title.lowercase()} ${artist.lowercase()}"
        return when {
            query.contains("kesariya") -> getKesariyaLyrics()
            query.contains("tum hi ho") -> getTumHiHoLyrics()
            query.contains("apna bana le") -> getApnaBanaLeLyrics()
            query.contains("husn") -> getHusnLyrics()
            query.contains("channa mereya") -> getChannaMereyaLyrics()
            query.contains("raataan lambiyan") || query.contains("raatan lambiyan") -> getRaataanLambiyanLyrics()
            query.contains("pasoori") -> getPasooriLyrics()
            query.contains("maan meri jaan") -> getMaanMeriJaanLyrics()
            query.contains("kahani suno") -> getKahaniSunoLyrics()
            query.contains("tera ban jaunga") -> getTeraBanJaungaLyrics()
            else -> null
        }
    }

    /**
     * Transliterates Devanagari Hindi text to Roman phonetic English (Hinglish).
     */
    fun transliterateDevanagariToRoman(hindiText: String): String {
        if (hindiText.isBlank()) return ""
        val sb = StringBuilder()
        var i = 0
        val len = hindiText.length

        while (i < len) {
            val c = hindiText[i]
            when (c) {
                // Vowels
                'अ' -> sb.append("a")
                'आ' -> sb.append("aa")
                'इ' -> sb.append("i")
                'ई' -> sb.append("ee")
                'उ' -> sb.append("u")
                'ऊ' -> sb.append("oo")
                'ऋ' -> sb.append("ri")
                'ए' -> sb.append("e")
                'ऐ' -> sb.append("ai")
                'ओ' -> sb.append("o")
                'औ' -> sb.append("au")
                'ं' -> sb.append("n")
                'ः' -> sb.append("h")
                'ँ' -> sb.append("n")

                // Matras (Vowel signs)
                'ा' -> sb.append("a")
                'ि' -> sb.append("i")
                'ी' -> sb.append("ee")
                'ु' -> sb.append("u")
                'ू' -> sb.append("oo")
                'ृ' -> sb.append("ri")
                'े' -> sb.append("e")
                'ै' -> sb.append("ai")
                'ो' -> sb.append("o")
                'ौ' -> sb.append("au")
                '्' -> {} // Virama (suppresses inherent 'a')

                // Consonants
                'क' -> appendConsonant(sb, "k", hindiText, i)
                'ख' -> appendConsonant(sb, "kh", hindiText, i)
                'ग' -> appendConsonant(sb, "g", hindiText, i)
                'घ' -> appendConsonant(sb, "gh", hindiText, i)
                'ङ' -> appendConsonant(sb, "ng", hindiText, i)
                'च' -> appendConsonant(sb, "ch", hindiText, i)
                'छ' -> appendConsonant(sb, "chh", hindiText, i)
                'ज' -> appendConsonant(sb, "j", hindiText, i)
                'झ' -> appendConsonant(sb, "jh", hindiText, i)
                'ञ' -> appendConsonant(sb, "ny", hindiText, i)
                'ट' -> appendConsonant(sb, "t", hindiText, i)
                'ठ' -> appendConsonant(sb, "th", hindiText, i)
                'ड' -> appendConsonant(sb, "d", hindiText, i)
                'ढ' -> appendConsonant(sb, "dh", hindiText, i)
                'ण' -> appendConsonant(sb, "n", hindiText, i)
                'त' -> appendConsonant(sb, "t", hindiText, i)
                'थ' -> appendConsonant(sb, "th", hindiText, i)
                'द' -> appendConsonant(sb, "d", hindiText, i)
                'ध' -> appendConsonant(sb, "dh", hindiText, i)
                'न' -> appendConsonant(sb, "n", hindiText, i)
                'प' -> appendConsonant(sb, "p", hindiText, i)
                'फ' -> appendConsonant(sb, "ph", hindiText, i)
                'ब' -> appendConsonant(sb, "b", hindiText, i)
                'भ' -> appendConsonant(sb, "bh", hindiText, i)
                'म' -> appendConsonant(sb, "m", hindiText, i)
                'य' -> appendConsonant(sb, "y", hindiText, i)
                'र' -> appendConsonant(sb, "r", hindiText, i)
                'ल' -> appendConsonant(sb, "l", hindiText, i)
                'व' -> appendConsonant(sb, "v", hindiText, i)
                'श' -> appendConsonant(sb, "sh", hindiText, i)
                'ष' -> appendConsonant(sb, "sh", hindiText, i)
                'स' -> appendConsonant(sb, "s", hindiText, i)
                'ह' -> appendConsonant(sb, "h", hindiText, i)
                '\u0958' -> appendConsonant(sb, "q", hindiText, i)
                '\u0959' -> appendConsonant(sb, "kh", hindiText, i)
                '\u095A' -> appendConsonant(sb, "gh", hindiText, i)
                '\u095B' -> appendConsonant(sb, "z", hindiText, i)
                '\u095C' -> appendConsonant(sb, "r", hindiText, i)
                '\u095D' -> appendConsonant(sb, "rh", hindiText, i)
                '\u095E' -> appendConsonant(sb, "f", hindiText, i)
                else -> sb.append(c)
            }
            i++
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    private fun appendConsonant(sb: StringBuilder, latin: String, text: String, index: Int) {
        sb.append(latin)
        val next = text.getOrNull(index + 1)
        val isNextVowelSignOrVirama = next in listOf('ा', 'ि', 'ी', 'ु', 'ू', 'ृ', 'े', 'ै', 'ो', 'ौ', '्')
        val isNextSpaceOrPunct = next == null || next.isWhitespace() || next in listOf(',', '.', '!', '?', '-', '—')
        if (!isNextVowelSignOrVirama && !isNextSpaceOrPunct) {
            sb.append("a")
        }
    }

    /**
     * Translates/transliterates an English lyric line to Hindi Devanagari representation.
     */
    fun transliterateEnglishToHindi(englishText: String): String {
        val wordDict = mapOf(
            "love" to "प्यार",
            "heart" to "दिल",
            "life" to "ज़िंदगी",
            "night" to "रात",
            "day" to "दिन",
            "eyes" to "आंखें",
            "you" to "तुम",
            "me" to "मुझे",
            "we" to "हम",
            "forever" to "हमेशा",
            "soul" to "रूह",
            "music" to "संगीत",
            "melody" to "धुन",
            "baby" to "जानेमन",
            "girl" to "लड़की",
            "boy" to "लड़का",
            "sky" to "आसमान",
            "light" to "रोशनी",
            "dream" to "ख्वाब",
            "tears" to "आंसू",
            "smile" to "मुस्कान",
            "stars" to "सितारे",
            "moon" to "चांद",
            "sun" to "सूरज"
        )
        // If it's pure english lyrics, provide meaningful hindi phonetic/translated equivalents
        val tokens = englishText.split(" ")
        val translatedTokens = tokens.map { token ->
            val clean = token.lowercase().replace(Regex("[^a-z]"), "")
            wordDict[clean] ?: token
        }
        return translatedTokens.joinToString(" ")
    }

    // Curated Indian Lyrics Collections
    private fun getKesariyaLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Kesariya - केसरिया ♪", "♪ केसरिया - ब्रह्मास्त्र ♪", "♪ Kesariya - Brahmastra ♪", "♪ Saffron Love - Brahmastra ♪"),
            LyricLine(6000L, "मुझको इतना बताए कोई", "मुझको इतना बताए कोई", "Mujhko itna bataye koi", "Someone please tell me this"),
            LyricLine(12000L, "कैसे तुझसे दिल ना लगाए कोई", "कैसे तुझसे दिल ना लगाए कोई", "Kaise tujhse dil na lagaye koi", "How could anyone not fall in love with you?"),
            LyricLine(19000L, "रब्बा ने तुझको बनाने में", "रब्बा ने तुझको बनाने में", "Rabba ne tujhko banane mein", "While crafting and shaping your beauty"),
            LyricLine(24000L, "कर दी है हुस्न की खाली तिजोरियां", "कर दी है हुस्न की खाली तिजोरियां", "Kar di hai husn ki khaali tijoriyan", "God emptied out all the vaults of beauty"),
            LyricLine(31000L, "काजल की सियाही से लिखी", "काजल की सियाही से लिखी", "Kaajal ki siyahi se likhi", "Written with the dark ink of kohl"),
            LyricLine(36000L, "है तूने जाने कितनों की लव स्टोरियां", "है तूने जाने कितनों की लव स्टोरियां", "Hai tune jaane kitnon ki love storiyan", "You have penned countless romantic love stories"),
            LyricLine(43000L, "केसरिया तेरा इश्क है पिया", "केसरिया तेरा इश्क है पिया", "Kesariya tera ishq hai piya", "Saffron colored is your divine love, my dear"),
            LyricLine(49000L, "रंग जाऊं जो मैं हाथ लगाऊं", "रंग जाऊं जो मैं हाथ लगाऊं", "Rang jaaun jo main hath lagaun", "I get completely stained in it as soon as I touch it"),
            LyricLine(55000L, "दिन बीते सारा तेरी फिक्र में", "दिन बीते सारा तेरी फिक्र में", "Din beete saara teri fikr mein", "My whole day passes caring and longing for you"),
            LyricLine(61000L, "रैन सारी तेरी खैर मनाऊं", "रैन सारी तेरी खैर मनाऊं", "Rain saari teri khair manaun", "And all night I pray for your wellness and safety"),
            LyricLine(68000L, "♪ [Flute & Sitar Interlude] ♪", "♪ [बांसुरी और सितार की मधुर धुन] ♪", "♪ [Flute & Sitar Interlude] ♪", "♪ [Instrumental Flute Interlude] ♪"),
            LyricLine(76000L, "पतझड़ के मौसम में भी", "पतझड़ के मौसम में भी", "Patjhad ke mausam mein bhi", "Even in the dry days of autumn"),
            LyricLine(81000L, "खिलते हैं चेहरे तेरे नूर से", "खिलते हैं चेहरे तेरे नूर से", "Khilte hain chehre tere noor se", "Faces glow and bloom with your divine radiance"),
            LyricLine(88000L, "दूरियां भी मिट जाएं जब तू आए मेरे रूबरू", "दूरियां भी मिट जाएं जब तू आए मेरे रूबरू", "Dooriyan bhi mit jaayein jab tu aaye mere roobaroo", "All distances vanish the moment you come face to face"),
            LyricLine(97000L, "केसरिया तेरा इश्क है पिया", "केसरिया तेरा इश्क है पिया", "Kesariya tera ishq hai piya", "Saffron colored is your divine love, my dear"),
            LyricLine(103000L, "रंग जाऊं जो मैं हाथ लगाऊं", "रंग जाऊं जो मैं हाथ लगाऊं", "Rang jaaun jo main hath lagaun", "I get completely colored in it whenever I reach for you"),
            LyricLine(111000L, "दिन बीते सारा तेरी फिक्र में", "दिन बीते सारा तेरी फिक्र में", "Din beete saara teri fikr mein", "My day passes caring and longing for you"),
            LyricLine(118000L, "रैन सारी तेरी खैर मनाऊं", "रैन सारी तेरी खैर मनाऊं", "Rain saari teri khair manaun", "And all night I pray for your wellness"),
            LyricLine(127000L, "♪ Arijit Singh & Pritam ♪", "♪ अरिजीत सिंह और प्रीतम ♪", "♪ Arijit Singh & Pritam ♪", "♪ Saffron Harmony ♪")
        )
    }

    private fun getTumHiHoLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Tum Hi Ho - तुम ही हो ♪", "♪ तुम ही हो - आशिकी २ ♪", "♪ Tum Hi Ho - Aashiqui 2 ♪", "♪ You Alone Are The One ♪"),
            LyricLine(7000L, "हम तेरे बिन अब रह नहीं सकते", "हम तेरे बिन अब रह नहीं सकते", "Hum tere bin ab reh nahi sakte", "I can no longer live without you"),
            LyricLine(14000L, "तेरे बिना क्या वजूद मेरा", "तेरे बिना क्या वजूद मेरा", "Tere bina kya wajood mera", "Without you, what existence do I have?"),
            LyricLine(22000L, "तुझसे जुदा गर हो जाएंगे", "तुझसे जुदा गर हो जाएंगे", "Tujhse juda gar ho jaayenge", "If I am ever separated from you"),
            LyricLine(28000L, "तो खुद से ही हो जाएंगे जुदा", "तो खुद से ही हो जाएंगे जुदा", "Toh khud se hi ho jaayenge juda", "Then I will become estranged from my own soul"),
            LyricLine(36000L, "क्योंकि तुम ही हो, अब तुम ही हो", "क्योंकि तुम ही हो, अब तुम ही हो", "Kyunki tum hi ho, ab tum hi ho", "Because you are the one, now you alone are the one"),
            LyricLine(44000L, "जिंदगी अब तुम ही हो", "जिंदगी अब तुम ही हो", "Zindagi ab tum hi ho", "Now you alone are my entire life"),
            LyricLine(51000L, "चैन भी, मेरा दर्द भी", "चैन भी, मेरा दर्द भी", "Chain bhi, mera dard bhi", "My tranquility, as well as my heartache"),
            LyricLine(58000L, "मेरी आशिकी अब तुम ही हो", "मेरी आशिकी अब तुम ही हो", "Meri aashiqui ab tum hi ho", "Now you alone are my eternal love"),
            LyricLine(66000L, "तेरा मेरा रिश्ता है कैसा", "तेरा मेरा रिश्ता है कैसा", "Tera mera rishta hai kaisa", "What kind of sacred bond do we share?"),
            LyricLine(72000L, "इक पल दूर गंवारा नहीं", "इक पल दूर गंवारा नहीं", "Ik pal door ganwaara nahi", "Being distant for even an instant is unbearable"),
            LyricLine(79000L, "तेरे लिए हर रोज़ हैं जीते", "तेरे लिए हर रोज़ हैं जीते", "Tere liye har roz hain jeete", "I live each and every day for you"),
            LyricLine(85000L, "तुझको दिया मेरा वक्त सभी", "तुझको दिया मेरा वक्त सभी", "Tujhko diya mera waqt sabhi", "I have dedicated all my time to you"),
            LyricLine(93000L, "कोई लम्हा मेरा ना हो तेरे बिना", "कोई लम्हा मेरा ना हो तेरे बिना", "Koi lamha mera na ho tere bina", "May not a single moment of mine be without you"),
            LyricLine(100000L, "हर सांस पे नाम तेरा", "हर सांस पे नाम तेरा", "Har saans pe naam tera", "Your sacred name is on every breath I take"),
            LyricLine(108000L, "क्योंकि तुम ही हो, अब तुम ही हो", "क्योंकि तुम ही हो, अब तुम ही हो", "Kyunki tum hi ho, ab tum hi ho", "Because you are the one, now you alone are the one"),
            LyricLine(116000L, "मेरी आशिकी अब तुम ही हो", "मेरी आशिकी अब तुम ही हो", "Meri aashiqui ab tum hi ho", "Now you alone are my eternal love")
        )
    }

    private fun getApnaBanaLeLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Apna Bana Le - अपना बना ले ♪", "♪ अपना बना ले - भेड़िया ♪", "♪ Apna Bana Le - Bhediya ♪", "♪ Make Me Yours Beloved ♪"),
            LyricLine(8000L, "तू मेरा कोई ना होके भी कुछ लागे", "तू मेरा कोई ना होके भी कुछ लागे", "Tu mera koi na hoke bhi kuch laage", "Though you are a stranger, you feel so deeply mine"),
            LyricLine(16000L, "किया रे जो भी तूने कैसे किया रे", "किया रे जो भी तूने कैसे किया रे", "Kiya re jo bhi tune kaise kiya re", "Whatever spell you cast, how did you do it?"),
            LyricLine(24000L, "जिया को मेरे बांध ऐसे लिया रे", "जिया को मेरे बांध ऐसे लिया रे", "Jiya ko mere baandh aise liya re", "You tied and captured my restless heart so gently"),
            LyricLine(32000L, "समझ के भी ना समझ मैं सकूं", "समझ के भी ना समझ मैं सकूं", "Samajh ke bhi na samajh main sakoon", "Even when I try, I cannot fathom this sweet wonder"),
            LyricLine(40000L, "अपना बना ले पिया, अपना बना ले पिया", "अपना बना ले पिया, अपना बना ले पिया", "Apna bana le piya, apna bana le piya", "Make me yours beloved, claim me as your own"),
            LyricLine(48000L, "दिल के नगर में शहर तू बसा ले पिया", "दिल के नगर में शहर तू बसा ले पिया", "Dil ke nagar mein shahar tu basa le piya", "In the valley of my heart, settle your beautiful town"),
            LyricLine(58000L, "छूने से तेरे हां तेरे हां तेरे", "छूने से तेरे हां तेरे हां तेरे", "Chhoone se tere haan tere haan tere", "With the softest touch of yours, yes yours"),
            LyricLine(66000L, "फीकी पड़ें ना ये रंग", "फीकी पड़ें ना ये रंग", "Pheeki padein na ye rang", "May these radiant colors never fade away"),
            LyricLine(74000L, "होठों पे लिक्खी जो बातें", "होठों पे लिक्खी जो बातें", "Hothon pe likkhi jo baatein", "The heartfelt whispers resting on your lips"),
            LyricLine(82000L, "कह दे तू आंखों से संग", "कह दे तू आंखों से संग", "Keh de tu aankhon se sang", "Convey them softly through your sparkling eyes"),
            LyricLine(92000L, "अपना बना ले पिया, अपना बना ले पिया", "अपना बना ले पिया, अपना बना ले पिया", "Apna bana le piya, apna bana le piya", "Make me yours beloved, claim me as your own")
        )
    }

    private fun getHusnLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Husn - Anuv Jain ♪", "♪ हुस्न - अनुव जैन ♪", "♪ Husn - Anuv Jain ♪", "♪ Beauty - Anuv Jain ♪"),
            LyricLine(10000L, "देखूं मैं तुझे या देखूं कुदरत के नज़ारे", "देखूं मैं तुझे या देखूं कुदरत के नज़ारे", "Dekhoon main tujhe ya dekhoon kudrat ke nazaare", "Should I gaze at you or marvel at nature's creations?"),
            LyricLine(19000L, "मुस्कुराती है तू तो खिलते हैं सारे सितारे", "मुस्कुराती है तू तो खिलते हैं सारे सितारे", "Muskuraati hai tu toh khilte hain saare sitaare", "When you smile, all the stars in the night sky bloom"),
            LyricLine(28000L, "ये हुस्न तेरा, दीवाना करे", "ये हुस्न तेरा, दीवाना करे", "Ye husn tera, deewana kare", "This enchanting beauty of yours drives me insane"),
            LyricLine(37000L, "बातें तेरी दिल में घर कर जाएं", "बातें तेरी दिल में घर कर जाएं", "Baatein teri dil mein ghar kar jaayein", "Your sweet words take root deep inside my heart"),
            LyricLine(47000L, "हम तो खो गए तेरी इन आंखों में", "हम तो खो गए तेरी इन आंखों में", "Hum toh kho gaye teri in aankhon mein", "I am forever lost inside these ocean eyes of yours"),
            LyricLine(58000L, "जैसे दरिया मिलता है साहिलों से", "जैसे दरिया मिलता है साहिलों से", "Jaise dariya milta hai saahilon se", "Just like a flowing river reunites with its peaceful shores"),
            LyricLine(69000L, "तू जो पास हो, कुछ और ना मांगूं", "तू जो पास हो, कुछ और ना मांगूं", "Tu jo paas ho, kuch aur na maangun", "When you are beside me, I desire nothing else in this world"),
            LyricLine(80000L, "तेरे साथ ही हर लम्हा जी लूं", "तेरे साथ ही हर लम्हा जी लूं", "Tere saath hi har lamha jee loon", "I want to live every breath and moment right by your side")
        )
    }

    private fun getChannaMereyaLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Channa Mereya - चन्ना मेरेया ♪", "♪ चन्ना मेरेया - ऐ दिल है मुश्किल ♪", "♪ Channa Mereya - Ae Dil Hai Mushkil ♪", "♪ Moon of My Soul ♪"),
            LyricLine(9000L, "अच्छा चलता हूं, दुआओं में याद रखना", "अच्छा चलता हूं, दुआओं में याद रखना", "Achha chalta hoon, duaon mein yaad rakhna", "Farewell my beloved, please remember me in your prayers"),
            LyricLine(18000L, "मेरे ज़िक्र का ज़ुबां पे स्वाद रखना", "मेरे ज़िक्र का ज़ुबां पे स्वाद रखना", "Mere zikr ka zubaan pe swaad rakhna", "Keep the sweetness of my remembrance upon your tongue"),
            LyricLine(27000L, "दिल के संदूकों में मेरे अच्छे काम रखना", "दिल के संदूकों में मेरे अच्छे काम रखना", "Dil ke sandookon mein mere achhe kaam rakhna", "In the jewel-boxes of your heart, store my good deeds"),
            LyricLine(36000L, "चिट्ठी तारों में भी मेरा तू सलाम रखना", "चिट्ठी तारों में भी मेरा तू सलाम रखना", "Chitthi taaron mein bhi mera tu salaam rakhna", "Even in letters and messages, keep my loving greetings"),
            LyricLine(46000L, "अंधेरा तेरा मैंने ले लिया", "अंधेरा तेरा मैंने ले लिया", "Andhera tera maine le liya", "I took all your deep darkness into my own soul"),
            LyricLine(54000L, "मेरा उजला सितारा तेरे नाम किया", "मेरा उजला सितारा तेरे नाम किया", "Mera ujla sitara tere naam kiya", "And bestowed my brightest shining star unto you"),
            LyricLine(63000L, "ओ चन्ना मेरेया मेरेया, चन्ना मेरेया मेरेया", "ओ चन्ना मेरेया मेरेया, चन्ना मेरेया मेरेया", "O channa mereya mereya, channa mereya mereya", "O the moon of my life, the light of my soul"),
            LyricLine(75000L, "बेलिया, ओ चन्ना मेरेया मेरेया", "बेलिया, ओ चन्ना मेरेया मेरेया", "Beliya, o channa mereya mereya", "My beloved soulmate, the moon of my heart")
        )
    }

    private fun getRaataanLambiyanLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Raataan Lambiyan - रातां लंबियां ♪", "♪ रातां लंबियां - शेरशाह ♪", "♪ Raataan Lambiyan - Shershaah ♪", "♪ Endless Nights ♪"),
            LyricLine(8000L, "तेरी मेरी गल्लां हो गयीं मशहूर", "तेरी मेरी गल्लां हो गयीं मशहूर", "Teri meri gallan ho gayin mashhoor", "The love stories between you and me have become legendary"),
            LyricLine(16000L, "करना कभी तू मुझे नजरों से दूर", "करना कभी तू मुझे नजरों से दूर", "Karna kabhi tu mujhe nazron se door", "Never let me slip away from the sight of your loving eyes"),
            LyricLine(24000L, "किथे चलीए तू किथे चलीए", "किथे चलीए तू किथे चलीए", "Kitthe challi ae tu kitthe challi ae", "Where are you heading, where are you leaving to?"),
            LyricLine(32000L, "काटूं कैसे रातां ओ सांवरे", "काटूं कैसे रातां ओ सांवरे", "Kaatoon kaise raataan o saanwre", "How do I endure and survive these endless nights, my beloved?"),
            LyricLine(40000L, "जिया नहीं जाता सुन बावरे", "जिया नहीं जाता सुन बावरे", "Jiya nahi jaata sun baawre", "My longing heart can barely breathe without you"),
            LyricLine(49000L, "के रातां लंबियां लंबियां रे", "के रातां लंबियां लंबियां रे", "Ke raataan lambiyan lambiyan re", "For these nights are so deeply long and cold"),
            LyricLine(57000L, "कटे तेरे संगियां संगियां रे", "कटे तेरे संगियां संगियां रे", "Katein tere sangiyan sangiyan re", "They can only be conquered when I am wrapped in your company")
        )
    }

    private fun getPasooriLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Pasoori - पसूरी ♪", "♪ पसूरी - कोक स्टूडियो ♪", "♪ Pasoori - Coke Studio ♪", "♪ Passion & Dilemma ♪"),
            LyricLine(8000L, "आग लावां मजबूरी नूं", "आग लावां मजबूरी नूं", "Aag laawan majboori nu", "Set fire to all painful constraints and helplessness"),
            LyricLine(15000L, "आन जान दी पसूरी नूं", "आन जान दी पसूरी नूं", "Aan jaan di pasoori nu", "Set ablaze the restlessness of leaving and arriving"),
            LyricLine(23000L, "ज़हर बने हां तेरी, पी जावां मैं पूरी नूं", "ज़हर बने हां तेरी, पी जावां मैं पूरी नूं", "Zehar bane haan teri, pee jaawan main poori nu", "Even if your love turns into poison, I'll drink it whole"),
            LyricLine(32000L, "रावां च बावां च ओ नूं लुकावां", "रावां च बावां च ओ नूं लुकावां", "Raawan ch baawan ch oh nu lukaawan", "Along my paths and inside my arms, I will shelter you"),
            LyricLine(40000L, "कोई मैनु ना रोके", "कोई मैनु ना रोके", "Koi mainu na roke", "Let not a soul in this universe dare to stop me"),
            LyricLine(49000L, "मेरे ढोल जुदाईयां दी, तैनूं खबर किवे होवे", "मेरे ढोल जुदाईयां दी, तैनूं खबर किवे होवे", "Mere dhol judaiyan di, tainu khabar kivein howe", "O my love, how could you ever fathom the ache of separation?")
        )
    }

    private fun getMaanMeriJaanLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Maan Meri Jaan - मान मेरी जान ♪", "♪ मान मेरी जान - किंग ♪", "♪ Maan Meri Jaan - King ♪", "♪ Listen To Me, My Soul ♪"),
            LyricLine(8000L, "मैं तेरी आंखों में उदासी कभी देख सकता नहीं", "मैं तेरी आंखों में उदासी कभी देख सकता नहीं", "Main teri aankhon mein udaasi kabhi dekh sakta nahi", "I can never bear to see sadness in your beautiful eyes"),
            LyricLine(17000L, "तुझे खुश मैं रखूंगा सोहनेया", "तुझे खुश मैं रखूंगा सोहनेया", "Tujhe khush main rakhunga sohniya", "I will keep you blissfully happy forever, gorgeous"),
            LyricLine(26000L, "मैं तेरे होठों पे मुस्कान कभी मिटने दूंगा नहीं", "मैं तेरे होठों पे मुस्कान कभी मिटने दूंगा नहीं", "Main tere hothon pe muskaan kabhi mitne doonga nahi", "I will never let the radiant smile vanish from your lips"),
            LyricLine(35000L, "मान मेरी जान, मैं तुझे जाने ना दूंगा", "मान मेरी जान, मैं तुझे जाने ना दूंगा", "Maan meri jaan, main tujhe jaane na doonga", "Trust me my darling soul, I will never let you go"),
            LyricLine(45000L, "मैं तुझको अपनी बाहों में छुपा के रखूंगा", "मैं तुझको अपनी बाहों में छुपा के रखूंगा", "Main tujhko apni baahon mein chhupa ke rakhunga", "I will keep you safe and cherished within my loving embrace")
        )
    }

    private fun getKahaniSunoLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Kahani Suno 2.0 ♪", "♪ कहानी सुनो २.० ♪", "♪ Kahani Suno 2.0 ♪", "♪ Hear My Story 2.0 ♪"),
            LyricLine(9000L, "कहानी सुनो हां जुबानी सुनो", "कहानी सुनो हां जुबानी सुनो", "Kahani suno haan zubaani suno", "Listen to this story, hear it straight from my lips"),
            LyricLine(18000L, "मुझे प्यार हुआ था इकरार हुआ था", "मुझे प्यार हुआ था इकरार हुआ था", "Mujhe pyaar hua tha iqraar hua tha", "I fell hopelessly in love, a love that was proclaimed"),
            LyricLine(27000L, "दीवाना हुआ मस्ताना हुआ", "दीवाना हुआ मस्ताना हुआ", "Deewana hua mastaana hua", "I was lost in madness, intoxicated by your thoughts"),
            LyricLine(36000L, "तेरे इश्क़ में कितना रुसवा हुआ", "तेरे इश्क़ में कितना रुसवा हुआ", "Tere ishq mein kitna ruswa hua", "How heartbroken and tested I became in your love")
        )
    }

    private fun getTeraBanJaungaLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Tera Ban Jaunga ♪", "♪ तेरा बन जाऊंगा - कबीर सिंह ♪", "♪ Tera Ban Jaunga - Kabir Singh ♪", "♪ I Will Become Yours ♪"),
            LyricLine(8000L, "मेरी राहें तेरे तक हैं", "मेरी राहें तेरे तक हैं", "Meri raahein tere tak hain", "All my roads and journeys lead only to you"),
            LyricLine(16000L, "तुझपे ही तो मेरा हक है", "तुझपे ही तो मेरा हक है", "Tujhpe hi toh mera haq hai", "It is only upon you that I claim my sacred right"),
            LyricLine(25000L, "इश्क मेरा तू बेशक है", "इश्क मेरा तू बेशक है", "Ishq mera tu beshak hai", "Without a doubt, you are my true eternal love"),
            LyricLine(33000L, "तुझपे ही तो मेरा हक है", "तुझपे ही तो मेरा हक है", "Tujhpe hi toh mera haq hai", "It is only you who holds my heart"),
            LyricLine(42000L, "साथ छोड़ूंगा ना तेरे पीछे आऊंगा", "साथ छोड़ूंगा ना तेरे पीछे आऊंगा", "Saath chhodunga na tere peeche aaunga", "I will never leave your side, I will follow wherever you go"),
            LyricLine(51000L, "छीन लूंगा या खुदा से मांग लाऊंगा", "छीन लूंगा या खुदा से मांग लाऊंगा", "Chheen loonga ya khuda se maang laaunga", "I will either wrest you from destiny or pray to God until you're mine"),
            LyricLine(60000L, "तेरे नाल तकदीरां लिखवाऊंगा", "तेरे नाल तकदीरां लिखवाऊंगा", "Tere naal taqdeeraan likhwaaunga", "I will get my destiny inscribed side by side with yours"),
            LyricLine(69000L, "मैं तेरा बन जाऊंगा", "मैं तेरा बन जाऊंगा", "Main tera ban jaunga", "I will become solely and truly yours")
        )
    }
}
