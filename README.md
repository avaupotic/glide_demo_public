# Glide - [GitHub](https://github.com/bumptech/glide)

## Kaj je Glide?

Glide je hitra knjižnica za upravljanje slik spisana primarno v Javi. Namenjena je preprostemu nalaganju, obdelavi in prikazovanju slik v Android aplikacijah.

## Prednosti

1. **Enostavna obdelava slik:** Slike je mogoče preprosto obdelati s funkcijami, kot so obrezovanje, spreminjanje velikosti, spreminjanje robov in druge manipulacije.

2. **Predpomnjenje slik:** Knjižnica upravlja s predpomnilnikom, kar pripomore k hitrejšemu prikazovanju slik in zmanjšanju obremenitve strežnika.

3. **Različni viri slik:** Glide lahko pridobiva slike iz različnih virov, vključno z lokalnim pomnilnikom, strežniki, spletni viri.

4. **Razumljiva sintaksa:** Uporablja razumljivo in intuitivno sintakso.
   
## Potencialne slabosti 

1. **Velikost knjižnice:** V primeru, kjer želimo maksimalno optimizirati velikost aplikacije.

2. **Kompatibilnost:** Problemi s kompatibilnostjo za starejše Android verzije.


## Osnovni primer uporabe (MyRecyclerViewAdapterDish)

```kotlin
    Glide.with(holder.itemView)
        .load(itemsViewModel.imgLink)
        .placeholder(R.drawable.dish_placeholder_white)
        .error(R.drawable.dish_placeholder_white)
        .into(holder.imageView)
```

## Primer urejanja slike

```kotlin        
    Glide.with(holder.itemView)
        .load(itemsViewModel.imgLink)
        .placeholder(R.drawable.dish_placeholder_white)
        .error(R.drawable.dish_placeholder_white)
        .apply(
            RequestOptions
            //.centerCropTransform() // Slika je popolnoma vidna brez whitespace-a
            .circleCropTransform() // Izreži sliko kot krog
            //.transform(RoundedCorners(16)) // Dodaj mehke robove
            //.override(300, 300) // Podaj širino in višino
        )
        .into(holder.imageView);
```

## Primer iz aplikacije

<table>
  <tr>
    <td>
      <img alt="Screenshot_1" height="1000px" src="./demo_images/rounded_vendors_screen.jpg" width="450px"/>
    </td>
    <td>
      <img alt="Screenshot_2" height="1000px" src="./demo_images/vendor_dishes_screen.jpg" width="450px"/>
    </td>
  </tr>
</table>


## Hitrost delovanja

<table>
  <tr>
    <td>
      <table>
        <caption>WEB</caption>
        <tr>
          <th>Resolucija Slike</th>
          <th>Hitrost(ms)</th>
        </tr>
        <tr>
          <td>HD (1980x1080)</td>
          <td>126</td>
        </tr>
        <tr>
          <td>2K (2560x1440)</td>
          <td>146</td>
        </tr>
        <tr>
          <td>4K (3840x2160)</td>
          <td>261</td>
        </tr>
      </table>
    </td>
    <td>
      <table>
        <caption>LOCAL</caption>
        <tr>
          <th>Resolucija Slike</th>
          <th>Hitrost(ms)</th>
        </tr>
        <tr>
          <td>HD (1980x1080)</td>
          <td>295</td>
        </tr>
        <tr>
          <td>2K (2560x1440)</td>
          <td>349</td>
        </tr>
        <tr>
          <td>4K (3840x2160)</td>
          <td>460</td>
        </tr>
      </table>
    </td>
  </tr>
</table>

ODSTOPANJA HITROSTI(za vsako ločljivost):<br>
WEB: +/- 20ms <br>
LOCAL: +/- 50ms

## Razširitve

Funkcije za transformacijo slik lahko razširimo s knjižnico [Glide-Transformations](https://github.com/shanescarlett/Glide-Transformations).

## Podatki

### Avtor: Sam Judd (GitHub - @sjudd)
### Št. razvijalcev: 146
### Zadnja verzija: Glide 5.0.0 ; 26. 9. 2023
### GitHub: Watch - 1k, Fork - 6.1k, Star - 34.1k
### Licence: BSD, part MIT and Apache 2.0 [Link](https://github.com/bumptech/glide/blob/master/LICENSE). 
