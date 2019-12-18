# LabManager
Sistema para controlar computadores em um laboratório de informática.

ESTE PROGRAMA É EXPERIMENTAL, E CASO VOCÊ O UTILIZE, ENTENDE QUE NÃO EXISTEM GARANTIAS. 
NÃO ME RESPONSABILIZO POR DANOS CAUSADOS PELA MÁ UTILIZAÇÃO DESTE SOFTWARE.

Como utilizar? Baixe a última release do projeto, e siga as instruções do leiame.txt

A comunicação entre os computadores utiliza um túnel SSL, por isso a configuração inicial irá gerar um certificado para o servidor ser identificado pelas máquinas cliente. Não é possível desativar a utilização do SSL, pois isso garante que ninguém na rede será capaz de se passar pelo servidor e controlar as máquinas ( se você se preocupa com a segurança deste programa, então não o utilize. ).

Caso queira, mande uma issue pedindo e então farei um vídeo tutorial de como instalar ( não vou fazer se ninguém pedir )

obs: o antivírus talvez identifique o programa cliente como vírus, em parte devido ao sistema de bloqueio de tela, em parte por utilizar o nome svchost.exe, portanto terá de criar uma exceção no antivírus.( só para ficar claro, é um falso-positivo ) 

# Compilar o projeto
Para executar este projeto você mesmo, precisa ter um pouco de conhecimento de Java e como utilizar o Netbeans.

Após baixar o repositório, abra cada um dos projetos do netbeans, e corrija as dependências que derem problema (os .jar estão em uma pasta jar)

Cada projeto tem um objetivo específico mas todos trabalham juntos para fazer o programa como um todo funcionar
- WebServer: é uma biblioteca que gerencia a parte de acessar e receber conexões via Socket. Também pode servir como Proxy
- LabStream: permite a transmissão de vídeos via UDP
- LabProxy: pequeno teste nada a ver, é um Proxy HTTP que permite impedir que certos sites abram
- LabManager: Projeto com as coisas em comum entre o programa Cliente e o Programa Servidor
- LabManager-Server: Projeto do programa que roda no lado servidor. este é o que controla os computadores
- LabManager-Client: Projeto do programa que roda no lado cliente. este está nos computadores, é controlado.
- LabManager-Install: Instalador que gerencia a parte de instalar como serviço e adiciona chaves no registro.

O LabManager-Client em deveria funcionar no linux, e em testes preliminares está tudo OK, mas ainda não foi testado em produção.
