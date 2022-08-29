Contributing guidelines
=======================

This docoument gathers important informations for potential contributors.

Specification Argument Resolver is an open-source library intially written an maintained as a hobby projecy by a single developer. Since its inception (2014), several developers contributed by reporting issues and creating Pull Requests. This is much appreciated and very helpful. At the same time, even a working code and Pull Request requires review and additional testing by the original author. Following guidelines will make the process much easier and allow merging PRs faster.

1. Follow the formatting conventions (check the indentation and spacing of the preexisting files). This sounds like not a big deal, but code formatted in a different way is much harder to review.
2. Don't change too much at once -- a coherent PR is easier to merge. If you change too many things in "architecture" or core concepts when impelementing a feature, then it will be very hard to review the PR.
3. Add unit tests, please. This project has very decent code coverage. It contains multiple unit tests and integration tests (by integration test in this context I mean a test that starts up a Spring context and creates a test controllers etc.). If you don't add unit tests, then I will have to write them myself -- and this is fine, but it will prolong the merging process significantly (as this is still just a hobby project for me).
4. Extend REAMDE if you add a new feature. This is crucial from the library user's perspective. If you don't extend the README, then I will have to do it myself -- and this is find, but it will prolong the merging process significantly (again -- this is just a hobby project for me, and I have (too) many other personal and profesional commitments).

Thank you very much for reading this! Thank you for all contributions. Open source for the win! 

Donations
---------

I have been maintaining this project since 2014 (8 years at the time of writing this document). A lot has changed in my life since then. I still try to contribute for free (being very thankful to the entire open source community), but it is just much harder then it used to be -- I have (too) many professional and personal commitments. If you find this library helpful and would like to make a donation, then you can use the PayPal link below. I don't expect anything, but thank you if you do this. It can help me justifying the time taken spent on maintaining this project or just buying a cup of coffe so that I don't fall asleep when coding at night :)

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/donate/?business=9GTMPKJ5X83US&no_recurring=1&item_name=Maintaining+specification-arg-resolver+library&currency_code=USD)
