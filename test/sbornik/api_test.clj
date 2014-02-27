(ns sbornik.api-test
  (:require [clojure.test :refer :all]
            [sbornik.api :refer :all]))

(def amos-excerpt
  "
#1
1| The words of Amos which came <i>to  him</i> in Accarim out of Thecue, which he saw concerning Jerusalem, in the days  of Ozias king of Juda, and in the days of Jeroboam the son of Joas king of  Israel, two years before the earthquake.
2| And he said, The Lord has spoken out  of Sion, and has uttered his voice out of Jerusalem; and the pastures of the  shepherds have mourned, and the top of Carmel is dried up.
3| And the Lord said, For three sins of  Damascus, and for four, I will not turn away from it; because they sawed with  iron saws the women with child of the Galaadites.
4|  And I will send a fire on the house of Azael, and it shall devour  the foundations of the son of Ader.
5|  And I will break to pieces the bars of Damascus, and will destroy  the inhabitants out of the plain of On, and will cut in pieces a tribe out of  the men of Charrhan: and the famous people of Syria shall be led captive, saith  the Lord.
6| Thus saith the Lord; For three sins of  Gaza, and for four, I will not turn away from them; because they took prisoners  the captivity of Solomon, to shut <i>them</i> up into Idumea.
7| And I will send forth a fire on the walls  of Gaza, and it shall devour its foundations.
8|  And I will destroy the inhabitants out of Azotus, and a tribe  shall be cut off from Ascalon, and I will stretch out my hand upon Accaron: and  the remnant of the Philistines shall perish, saith the Lord.
9| Thus saith the Lord; For three  transgressions of Tyre, and for four, I will not turn away from it; because they  shut up the prisoners of Solomon into Idumea, and remembered not the covenant of  brethren.
10| And I will send forth a  fire on the walls of Tyre, and it shall devour the foundations of it.
11| Thus saith the Lord; For three sins  of Idumea, and for four, I will not turn away from them; because they pursued  their brother with the sword, and destroyed the mother upon the earth, and  summoned up his anger for a testimony, and kept up his fury to the end.
12| And I will send forth a fire upon  Thaman, and it shall devour the foundations of her walls.
13| Thus saith the Lord; For three sins  of the children of Ammon, and for four, I will not turn away from him; because  they ripped up the women with child of the Galaadites, that they might widen  their coasts.
14| And I will kindle a  fire on the walls of Rabbath, and it shall devour her foundations with shouting  in the day of war, and she shall be shaken in the days of her destruction:
15| and her kings shall go into  captivity, their priests and their rulers together, saith the Lord. Chapter  2

#2
1| Thus saith the Lord; For three sins  of Moab, and for four, I will not turn away from it; because they burnt the  bones of the king of Idumea to lime.
2|  But I will send forth a fire on Moab, and it shall devour the  foundations of its cities: and Moab shall perish in weakness, with a shout, and  with the sound of a trumpet.
3| And I  will destroy the judge out of her, and slay all her princes with him, saith the  Lord.
")

(def bible-book-resource (java.io.StringReader. amos-excerpt))

(deftest test-bible-lines
  (let [lines (bible-lines bible-book-resource [1 1] [2 3])]
    (is (= (count lines) 21))
    (is (= (first lines) "#1"))
    (is (= (last lines)
           "3| And I  will destroy the judge out of her, and slay all her princes with him, saith the  Lord."))))

(deftest ^:integration test-bible-excerpt
  (let [excerpt (bible-excerpt {:lang "en"
                                :edition "brenton"
                                :book "Amos"
                                :start [1 1]
                                :end [2 3]})]
    (is (re-find #"The words of Amos which came" excerpt))
    (is (re-find #"And I  will destroy the judge out of her" excerpt))))
